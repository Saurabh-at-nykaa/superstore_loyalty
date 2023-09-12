package com.nykaa.loyalty.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nykaa.base.enums.Domain;
import com.nykaa.base.exception.NykaaWebServiceException;
import com.nykaa.loyalty.dto.CustomerOfferMappingDto;
import com.nykaa.loyalty.dto.OfferRuleDetailsDto;
import com.nykaa.loyalty.dto.QueryOfferMappingDto;
import com.nykaa.loyalty.dto.TierDetails;
import com.nykaa.loyalty.dto.UserEarningDetails;
import com.nykaa.loyalty.dto.UserLoyaltyDetailsDto;
import com.nykaa.loyalty.dto.UserOfferDetails;
import com.nykaa.loyalty.dto.UserOfferResponseDto;
import com.nykaa.loyalty.dto.UserSegmentQuery;
import com.nykaa.loyalty.entity.LoyaltyCustomerOfferMapping;
import com.nykaa.loyalty.entity.OfferRuleDetails;
import com.nykaa.loyalty.entity.OrderDetails;
import com.nykaa.loyalty.entity.PincodeClusterMapping;
import com.nykaa.loyalty.enums.CatalogBrandParams;
import com.nykaa.loyalty.enums.CustomerType;
import com.nykaa.loyalty.enums.EmailParams;
import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.enums.OfferType;
import com.nykaa.loyalty.enums.OrderStatus;
import com.nykaa.loyalty.enums.RewardPointsCreditStatus;
import com.nykaa.loyalty.enums.RewardPointsExpiryUnit;
import com.nykaa.loyalty.enums.RewardPointsType;
import com.nykaa.loyalty.enums.TargetType;
import com.nykaa.loyalty.exception.LoyaltyException;
import com.nykaa.loyalty.mapper.OfferRuleDetailsMapper;
import com.nykaa.loyalty.repository.master.LoyaltyCustomerOfferMappingRepository;
import com.nykaa.loyalty.repository.master.OfferRuleDetailsRepository;
import com.nykaa.loyalty.repository.read.LoyaltyCustomerOfferMappingReadRepository;
import com.nykaa.loyalty.repository.read.OfferRuleDetailsReadRepository;
import com.nykaa.loyalty.repository.read.OrderDetailsReadRepository;
import com.nykaa.loyalty.repository.read.PincodeClusterMappingReadRepository;
import com.nykaa.loyalty.service.EventSchedulerService;
import com.nykaa.loyalty.service.OfferRuleDetailsService;
import com.nykaa.loyalty.service.S3Service;
import com.nykaa.loyalty.service.helper.RedshiftQueryHelper;
import com.nykaa.loyalty.util.CommonUtils;
import com.nykaa.loyalty.util.Constants;
import com.nykaa.loyalty.util.DateUtil;
import com.nykaa.loyalty.util.EmailUtil;
import com.nykaa.loyalty.util.SystemPropertyUtil;
import com.nykaa.superstore.loyalty.dto.LoyaltyEventRequestDto;
import com.nykaa.superstore.loyalty.dto.NewUserEventDTO;
import com.nykaa.superstore.loyalty.enums.EventType;
import com.nykaa.vault.dto.CreateTransactionDTO;
import com.nykaa.vault.dto.TransactionDTO;
import com.nykaa.vault.enums.TransactionType;
import com.nykaa.vault.enums.VaultType;
import com.nykaa.vault.response.ResponseBean;
import com.nykaa.vault.service.VaultClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OfferRuleDetailServiceImpl implements OfferRuleDetailsService {

    @Autowired
    private OfferRuleDetailsRepository offerRuleDetailsRepository;

    @Autowired
    private OfferRuleDetailsReadRepository offerRuleDetailsReadRepository;

    @Autowired
    private LoyaltyCustomerOfferMappingRepository loyaltyCustomerOfferMappingRepository;

    @Autowired
    private LoyaltyCustomerOfferMappingReadRepository loyaltyCustomerOfferMappingReadRepository;

    @Autowired
    private OfferRuleDetailsMapper offerRuleDetailsMapper;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private RedshiftQueryHelper queryHelper;

    @Autowired
    private EventSchedulerService eventSchedulerService;

    @Autowired
    @Qualifier("loyaltyObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private OrderDetailsReadRepository orderDetailsReadRepository;

    @Autowired
    private VaultClientService vaultClientService;

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    private PincodeClusterMappingReadRepository pincodeClusterMappingReadRepository;

    private static Map<String, List<String>> pincodeClusterMap;

    @PostConstruct
    public void init() {
        this.populatePincodeClusterMap();
    }

    private void populatePincodeClusterMap() {
        pincodeClusterMap = new HashMap<>();
        List<PincodeClusterMapping> pincodeClusterMappingList = pincodeClusterMappingReadRepository.findByDeleted(false);
        for (PincodeClusterMapping elem : pincodeClusterMappingList) {
            if (pincodeClusterMap.containsKey(elem.getCluster())) {
                pincodeClusterMap.get(elem.getCluster()).add(elem.getPincode());
            } else {
                List<String> pincodeList = new ArrayList<>();
                pincodeList.add(elem.getPincode());
                pincodeClusterMap.put(elem.getCluster(), pincodeList);
            }
        }
    }

    @Override
    public OfferRuleDetailsDto createOfferRule(@Valid OfferRuleDetailsDto offerRuleDetailsDto) throws LoyaltyException {
        offerRuleDetailsDto.setStartDate(DateUtil.getOfferStartDate(offerRuleDetailsDto.getStartDate()));
        offerRuleDetailsDto.setEndDate(DateUtil.getOfferEndDate(offerRuleDetailsDto.getEndDate()));
        if (offerRuleDetailsDto.getRewardPointType().equals(RewardPointsType.FLAT)) {
            int size = CommonUtils.getNoOfConfiguredTiers(offerRuleDetailsDto.getTargetValues());
            offerRuleDetailsDto.setMaxReward(Double.valueOf(offerRuleDetailsDto.getRewardPointValues().get(size - 1)));
        }
        Optional<OfferRuleDetails> optionalEntity = offerRuleDetailsReadRepository.findByOfferRuleName(offerRuleDetailsDto.getOfferRuleName());
        if (optionalEntity.isPresent()) {
            throw new LoyaltyException(ErrorCodes.OFFER_RULE_NAME_ALREADY_EXISTS);
        }
        OfferRuleDetails offerRuleDetails = offerRuleDetailsMapper.dtoToEntity(offerRuleDetailsDto);
        OfferRuleDetails savedEntity = offerRuleDetailsRepository.save(offerRuleDetails);
        // NEW_USER customer type offers are not required to be scheduled
        Date startOfferEventDate = DateUtil.getOfferStartEventDate(savedEntity.getStartDate());
        log.info("Scheduling start event for offer id {} at {}", savedEntity.getId(), startOfferEventDate);
        if (!CustomerType.NEW_USER.equals(savedEntity.getCustomerType())) {
            eventSchedulerService.scheduleLoyaltyOfferEvent(new LoyaltyEventRequestDto(savedEntity.getId(),
                    EventType.OFFER_START, 0, startOfferEventDate));
        }
      //scheduling event for loyalty end date
        Date endOfferEventDate = DateUtil.getOfferEndEventDate(savedEntity.getEndDate());
        log.info("Scheduling end event for offer id {} at {}", savedEntity.getId(), endOfferEventDate);
      eventSchedulerService.scheduleLoyaltyOfferEvent(
              new LoyaltyEventRequestDto(savedEntity.getId(), EventType.OFFER_END, 0, endOfferEventDate));
        return offerRuleDetailsMapper.entityToDto(savedEntity);
    }

    @Override
    public OfferRuleDetailsDto updateOfferRule(@Valid OfferRuleDetailsDto offerRuleDetailsDto) {
        Assert.notNull(offerRuleDetailsDto.getId(), ErrorCodes.ID_MISSING.getMessage());
        Optional<OfferRuleDetails> optionalEntity = offerRuleDetailsReadRepository.findById(offerRuleDetailsDto.getId());
        if (!optionalEntity.isPresent()) {
            throw new LoyaltyException(ErrorCodes.OFFER_RULE_NOT_FOUND);
        }
        if (offerRuleDetailsDto.getRewardPointType().equals(RewardPointsType.FLAT)) {
            int size = CommonUtils.getNoOfConfiguredTiers(offerRuleDetailsDto.getTargetValues());
            offerRuleDetailsDto.setMaxReward(Double.valueOf(offerRuleDetailsDto.getRewardPointValues().get(size - 1)));
        }
        OfferRuleDetails entity = optionalEntity.get();
        entity = offerRuleDetailsMapper.dtoToEntity(offerRuleDetailsDto);
        OfferRuleDetails updatedEntity = offerRuleDetailsRepository.save(entity);
        return offerRuleDetailsMapper.entityToDto(updatedEntity);
    }

    @Override
    public OfferRuleDetailsDto getOfferRuleById(Long offerRuleId) {
        Optional<OfferRuleDetails> offerRuleDetails = offerRuleDetailsReadRepository.findById(offerRuleId);
        if (!offerRuleDetails.isPresent()) {
            throw new LoyaltyException(ErrorCodes.OFFER_RULE_NOT_FOUND);
        }
        return offerRuleDetailsMapper.entityToDto(offerRuleDetails.get());
    }

    @Override
    public OfferRuleDetailsDto getOfferRuleByName(String offerName) {
        Optional<OfferRuleDetails> offerRuleDetails = offerRuleDetailsReadRepository.findByOfferRuleName(offerName);
        if (!offerRuleDetails.isPresent()) {
            throw new LoyaltyException(ErrorCodes.OFFER_RULE_NOT_FOUND);
        }
        return offerRuleDetailsMapper.entityToDto(offerRuleDetails.get());
    }

    @Override
    public Page<OfferRuleDetailsDto> getOfferRules(Pageable pageable) {
        Page<OfferRuleDetails> entities = offerRuleDetailsReadRepository.findAll(pageable);
        return entities.map(offerRuleDetailsMapper::entityToDto);
    }

    @Override
    public Set<String> getUniqueClusters() {
        return pincodeClusterMap.keySet();
    }

    @Override
    public Map<Long, String> getCustomerGroups() {
        return SystemPropertyUtil.getCustomerGroups();
    }

    @Override
    public Map<Long, String> getBrands() {
        Map<Long, String> brands = new HashMap<>();
        List<CSVRecord> records = s3Service.getCsvFile(SystemPropertyUtil.getCatalogBucket(), SystemPropertyUtil.getBrandFileKey());
        if (CollectionUtils.isNotEmpty(records)) {
            for (CSVRecord record : records) {
                String brandId = record.get(CatalogBrandParams.BRAND_ID.getName());
                String brandName = record.get(CatalogBrandParams.BRAND_NAME.getName());
                if (StringUtils.isNotBlank(brandId) && StringUtils.isNotBlank(brandName)) {
                    brands.put(Long.valueOf(brandId), brandName);                    
                }
            }
        }
        return brands;
    }

    @Override
    public void toggleStatus(Long id, boolean status) {
        Optional<OfferRuleDetails> optionalEntity = offerRuleDetailsReadRepository.findById(id);
        if (!optionalEntity.isPresent()) {
            throw new LoyaltyException(ErrorCodes.OFFER_RULE_NOT_FOUND);
        }
        OfferRuleDetails entity = optionalEntity.get();
        if (entity.getStartDate().before(new Date())) {
            throw new LoyaltyException(ErrorCodes.UPDATE_NOT_ALLOWED);
        }
        entity.setIsActive(status);
        offerRuleDetailsRepository.save(entity);
    }

    @Override
    public List<UserLoyaltyDetailsDto> getCurrentLoyaltyOffers(String customerId) {
        List<UserLoyaltyDetailsDto> loyaltyDetailsDtoList = new ArrayList<>();
        List<LoyaltyCustomerOfferMapping> currentMapping = loyaltyCustomerOfferMappingReadRepository
                .findByCustomerIdAndRunning(customerId, new Date());
        if (CollectionUtils.isNotEmpty(currentMapping)) {
            for (LoyaltyCustomerOfferMapping loyaltyCustomerMap: currentMapping){
                UserLoyaltyDetailsDto userLoyaltyDetailsDto = createUserLoyaltyDetail(loyaltyCustomerMap.getOfferId(),
                        loyaltyCustomerMap.getOfferTypeUserResult());
                if (null != userLoyaltyDetailsDto) {
                    userLoyaltyDetailsDto.setCurrentProgress(loyaltyCustomerMap.getCurrentProgress());
                    loyaltyDetailsDtoList.add(userLoyaltyDetailsDto);
                }
            }
        }
        return loyaltyDetailsDtoList;
    }

    @Override
    public Map<String, Object> validateAndGetQueryCount(OfferRuleDetailsDto offerDetails) {
        Map<String, Object> responseAttributes = new HashMap<>();
        List<String> validationErrors = validateOfferDetails(offerDetails);
        if (CollectionUtils.isEmpty(validationErrors)) {
            getQueryByCustomerType(offerDetails, responseAttributes, validationErrors);
        }
        responseAttributes.put(Constants.AdminPanel.ERRORS, validationErrors);
        return responseAttributes;
    }

    @Override
    public void mapNewUserToOffer(NewUserEventDTO newUserEventDTO) {
        validateRequest(newUserEventDTO);
        List<OfferRuleDetails> newUserOfferList = offerRuleDetailsReadRepository
                .findByCustomerTypeAndIsActiveAndStartDateLessThanAndEndDateGreaterThan(CustomerType.NEW_USER,
                        true, newUserEventDTO.getKycApproveDate(), newUserEventDTO.getKycApproveDate());
        if (CollectionUtils.isNotEmpty(newUserOfferList)) {
            for (OfferRuleDetails offer : newUserOfferList) {
                LoyaltyCustomerOfferMapping newEntry =
                        createOfferMappingEntryForNewUser(offer, newUserEventDTO.getCustomerId());
                loyaltyCustomerOfferMappingRepository.save(newEntry);
            }
        } else {
            log.info("No new_user_offer is running at the moment");
        }
    }

    @Override
    @Async
    public void startLoyaltyOffer(LoyaltyEventRequestDto requestDto) throws Exception{
        Long offerId = requestDto.getOfferId();
        Optional<OfferRuleDetails> offerRuleDetails = offerRuleDetailsReadRepository.findByIdAndIsActive(offerId, true);
        if (!offerRuleDetails.isPresent()) {
            log.error("Offer rule cannot be found");
            throw new LoyaltyException(ErrorCodes.OFFER_RULE_NOT_FOUND);
        }
        OfferRuleDetails loyaltyOffer = offerRuleDetails.get();
        Date currentDate = new Date();
        if (currentDate.after(loyaltyOffer.getEndDate())) {
            log.error("Offer end date surpassed current date");
            throw new LoyaltyException(ErrorCodes.OFFER_END_DATE_SURPASSED);
        }
        log.info("Executing customer target query for offer_rule_id : {}, query : {}", offerId, loyaltyOffer.getQuery());
        ResultSet resultSet;
        try {
            resultSet = queryHelper.executeQueryAndReturnResult(loyaltyOffer.getQuery());
        } catch (LoyaltyException ex) {
            if (ErrorCodes.REDSHIFT_QUERY_TIMEOUT.equals(ex.getErrorCode())) {
                log.info("Redshift query timed out retrying");
                retryLoyaltyStartEvent(requestDto);
                log.info("Retry offer start event is successfully scheduled for offerId : {}", requestDto.getOfferId());
                return;
            } else {
                throw ex;
            }
        }
        log.info("query executed successfully for offer_rule_id {}", offerId);
        List<String> alreadyMappedCustomers = new ArrayList<>();
        List<LoyaltyCustomerOfferMapping> newMappings = new ArrayList<>();
        if (resultSet != null) {
            while (resultSet.next()) {
                String customerId = String.valueOf(resultSet.getLong(Constants.Alias.CUSTOMER_ID));
                if (!checkForExistingMapping(customerId, loyaltyOffer)) {
                    log.info("existing mapping found for customer id {} and offer id {}", customerId, offerId);
                    alreadyMappedCustomers.add(customerId);
                } else {
                    String userPrevOfferTypeResult = CustomerType.TRANSACTED.equals(loyaltyOffer.getCustomerType()) ?
                            getPrevCycleSpendValueByType(loyaltyOffer.getOfferType(), resultSet) : null;
                    newMappings.add(createOfferMappingEntry(loyaltyOffer, customerId, userPrevOfferTypeResult));
                }
            }
        }
        log.info("attaching offer {} to {} customers", loyaltyOffer.getOfferRuleName(), newMappings.size());
        loyaltyOffer.setMappingDone(Boolean.TRUE);
        offerRuleDetailsRepository.save(loyaltyOffer);
        loyaltyCustomerOfferMappingRepository.saveAll(newMappings);
        if (CollectionUtils.isNotEmpty(alreadyMappedCustomers)) {
            log.info("{} already mapped customers are found for offer {}, sending mail to inform.", alreadyMappedCustomers.size(),
                    loyaltyOffer.getOfferRuleName());
            sendMailResponse(alreadyMappedCustomers, loyaltyOffer);
        }
    }

    private String getPrevCycleSpendValueByType(OfferType offerType, ResultSet resultSet) {
        try {
            switch (offerType) {
                case TARGET_SPEND:
                    return resultSet.getString(Constants.Alias.AVG_SPEND);
                case NO_OF_ORDERS:
                    return resultSet.getString(Constants.Alias.TOTAL_ORDER);
                case UNIQUE_LINE_CUTS:
                    return resultSet.getString(Constants.Alias.UNIQUE_CUTS);
                case UNIQUE_BRANDS:
                    return resultSet.getString(Constants.Alias.UNIQUE_BRANDS);
                default:
                    throw new LoyaltyException(ErrorCodes.OFFER_CUSTOMER_MAPPING_ERROR);
            }
        } catch (Exception e) {
            log.error("Invalid column name used or db connection failed");
            throw new LoyaltyException(ErrorCodes.OFFER_CUSTOMER_MAPPING_ERROR);
        }
    }

    private LoyaltyCustomerOfferMapping createOfferMappingEntry(OfferRuleDetails offer, String customerId,
                                                                String userPrevResultValue) {
        log.info("creating new mapping for customer id {} and offer id {}", customerId, offer.getId());
        LoyaltyCustomerOfferMapping newMapping = new LoyaltyCustomerOfferMapping();
        newMapping.setCustomerId(customerId);
        newMapping.setOfferId(offer.getId());
        newMapping.setStartDate(offer.getStartDate());
        newMapping.setEndDate(offer.getEndDate());
        // todo column name to be consistent
        newMapping.setOfferTypeUserResult(userPrevResultValue);
        newMapping.setCurrentProgress(setCurrentProgressByType(offer.getOfferType()));
        newMapping.setCreditStatus(RewardPointsCreditStatus.PENDING);
        log.info("created new mapping for customer id {} and offer id {}", customerId, offer.getId());
        return newMapping;
    }

    private String setCurrentProgressByType(OfferType offerType) {
        switch (offerType) {
            case TARGET_SPEND:
                return "0.0";
            case NO_OF_ORDERS:
            case UNIQUE_BRANDS:
            case UNIQUE_LINE_CUTS:
                return "0";
        }
        return null;
    }

    private boolean checkForExistingMapping(String customerId, OfferRuleDetails offer) {
        log.info("checking existing mapping for customer id {} and offer id {}", customerId, offer.getId());
        List<LoyaltyCustomerOfferMapping> existingMapping = loyaltyCustomerOfferMappingReadRepository
                .findByCustomerIdAndOfferDurationInRange(customerId, offer.getStartDate(), offer.getEndDate());
        if (CollectionUtils.isEmpty(existingMapping)) {
            log.info("no existing mapping for customer id {}", customerId);
            return true;
        }
        for (LoyaltyCustomerOfferMapping mapping : existingMapping) {
            Optional<OfferRuleDetails> existingOfferOptional = offerRuleDetailsReadRepository.findById(mapping.getOfferId());
            OfferRuleDetails existingOffer;
            if (existingOfferOptional.isPresent() && existingOfferOptional.get().getIsActive()) {
                existingOffer = existingOfferOptional.get();
                if (existingOffer.getOfferType().equals(offer.getOfferType())
                        && existingOffer.getNykaaShare().equals(offer.getNykaaShare())
                        && existingOffer.getBrandShare().equals(offer.getBrandShare())) {
                    log.info("existing mapping for customer id {} and offer id {}", customerId, offer.getId());
                    return false;
                }
            }
        }
        log.info("no existing mapping for customer id {}", customerId);
        return true;
    }

    private List<String> validateOfferDetails(OfferRuleDetailsDto offerDetails) {
        if (offerDetails.getRewardPointType().equals(RewardPointsType.FLAT)) {
            int size = CommonUtils.getNoOfConfiguredTiers(offerDetails.getTargetValues());
            offerDetails.setMaxReward(Double.valueOf(offerDetails.getRewardPointValues().get(size - 1)));
        }
        List<String> validationErrors = new ArrayList<>();
        if (Objects.isNull(offerDetails.getId())) {
            Optional<OfferRuleDetails> optionalEntity = offerRuleDetailsReadRepository
                    .findByDomainAndOfferTypeAndStartDateAndEndDateAndBrandShareAndNykaaShareAndCustomerType(
                            offerDetails.getDomain().name(), offerDetails.getOfferType().name(),
                            DateUtil.getOfferStartDate(offerDetails.getStartDate()),
                            DateUtil.getOfferEndDate(offerDetails.getEndDate()), offerDetails.getBrandShare(),
                            offerDetails.getNykaaShare(), offerDetails.getUserSegmentQuery().getCustomerType().name());
            if (optionalEntity.isPresent()) {
                validationErrors.add(Constants.ValidationErrors.SAME_OFFER_EXISTS);
            }
        }
        if (offerDetails.getBrandShare() + offerDetails.getNykaaShare() != 100) {
            validationErrors.add(Constants.ValidationErrors.INVALID_FUNDING_SPLIT);
        }
        if (!offerDetails.getOfferType().equals(OfferType.TARGET_SPEND)
                && offerDetails.getTargetType().equals(TargetType.VARIABLE)) {
            validationErrors.add(Constants.ValidationErrors.VARIABLE_TARGETS_NOT_ALLOWED);
        }
        Double maxRewardValue = 0.0;
        Double maxTargetValue = 0.0;
        boolean isRewardValid = true;
        boolean isTargetValid = true;
        boolean isIntegerTargetRequired = false;

        for (int i = 0; i < offerDetails.getTargetValues().size(); i++) {
            if (!isRewardValid || !isTargetValid) {
                validationErrors.add(Constants.ValidationErrors.INVALID_TIER_VALUES);
            }
            if (isIntegerTargetRequired) {
                validationErrors.add(Constants.ValidationErrors.FRACTIONAL_TARGET_NOT_ALLOWED);
            }
            if (!isRewardValid || !isTargetValid || isIntegerTargetRequired) {
                break;
            }
            Double currentRewardValue = 0.0;
            Double currentTargetValue = 0.0;
            if (NumberUtils.isParsable(offerDetails.getRewardPointValues().get(i))) {
                currentRewardValue = Double.parseDouble(offerDetails.getRewardPointValues().get(i));
                if (currentRewardValue > maxRewardValue) {
                    maxRewardValue = currentRewardValue;
                } else {
                    isRewardValid = false;
                }
            } else {
                currentRewardValue = 0.0;
                isRewardValid = false;
            }

            if (NumberUtils.isParsable(offerDetails.getTargetValues().get(i))) {
                currentTargetValue = Double.parseDouble(offerDetails.getTargetValues().get(i));
                if (offerDetails.getTargetType().equals(TargetType.VARIABLE) && currentTargetValue < 1) {
                    validationErrors.add(Constants.ValidationErrors.INVALID_TARGET_FACTOR);
                    break;
                }
                if (offerDetails.getTargetType().equals(TargetType.FIXED)
                        && !offerDetails.getOfferType().equals(OfferType.TARGET_SPEND)
                        && !StringUtils.isNumeric(offerDetails.getTargetValues().get(i))) {
                    isIntegerTargetRequired = true;
                }

                if (currentTargetValue > maxTargetValue) {
                    maxTargetValue = currentTargetValue;
                } else {
                    isTargetValid = false;
                }
            } else {
                currentTargetValue = 0.0;
                isTargetValid = false;
            }
        }
        if (offerDetails.getRewardPointType().equals(RewardPointsType.FLAT)
                && maxRewardValue > offerDetails.getMaxReward()) {
            validationErrors.add(Constants.ValidationErrors.FIXED_REWARD_GREATER_THAN_MAX_REWARD);
        }
        return validationErrors;
    }

    private void getQueryByCustomerType(OfferRuleDetailsDto offerDetails, Map<String, Object> responseAttributes,
            List<String> validationErrors) {
        if (Objects.nonNull(offerDetails.getId())) {
            return;
        }
        String query = null;
        String countQuery =  null;
        try {
            switch (offerDetails.getUserSegmentQuery().getCustomerType()) {
            case LIFETIME_DORMANT:
                query = buildLifetimeDormantCustomerQuery();
                countQuery = query.replace("select distinct ndciv.customer_id", "select COUNT(distinct ndciv.customer_id)");
                break;
            case TRANSACTED:
                query = buildTransactedQuery(offerDetails.getUserSegmentQuery());
                //TODO Optimise count query
                StringBuilder countQueryBuilder = new StringBuilder("select count(*) from (");
                countQueryBuilder.append(query.replace(" ; ", StringUtils.EMPTY)).append(" );");
                countQuery = countQueryBuilder.toString();
                break;
            case NEW_USER:
                offerDetails.setNewUserRule(true);
                return;
            default:
                break;
            }
            if (Constants.Environment.PREPROD.equalsIgnoreCase(profile)) {
                query = convertQueryByEnv(query);
                countQuery = convertQueryByEnv(countQuery);
            }
            log.info("Select query for customer id: {}", query);
            int queryCount = getQueryCount(countQuery);
            responseAttributes.put(Constants.AdminPanel.QUERY_COUNT, queryCount);
            responseAttributes.put(Constants.AdminPanel.QUERY, query);
        } catch (JsonProcessingException | ParseException e) {
            log.error(ErrorCodes.QUERY_BUILDING_ERROR.getMessage(), e.getMessage());
            validationErrors.add(ErrorCodes.QUERY_BUILDING_ERROR.getMessage());
        }
    }

    private String convertQueryByEnv(String query) {
        query = query.replaceAll("beauty.nykaad_customer_info_view", "devops.nykaad_customer_info_view_beauty");
        query = query.replaceAll("beauty.fact_order_view", "devops.fact_order_view");
        query = query.replaceAll("beauty.address_info", "devops.address_info_beauty");
        return query;
    }

    private int getQueryCount(String countQuery) {
        log.info("count query: {}", countQuery);
        try {
            int count = 0;
            ResultSet resultSet = queryHelper.executeQueryAndReturnResult(countQuery);
            if (resultSet != null) {
                while (resultSet.next()) {
                     count = resultSet.getInt(1);
                }
            }
            log.info("found {} user for the count query {}", count, countQuery);
            return count;
        } catch (Exception se) {
            log.error("Exception occurred during query execution : {}", se.getMessage());
            throw new LoyaltyException(ErrorCodes.REDSHIFT_CONNECTION_ERROR);
        }
    }

    private String buildLifetimeDormantCustomerQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(
                "select distinct ndciv.customer_id from beauty.nykaad_customer_info_view ndciv where ndciv.status = 3 and ndciv.customer_id not in ");
        queryBuilder.append(
                "(select distinct fov.order_customerid from beauty.fact_order_view fov where lower(fov.order_source) = 'nykaad');");
        return queryBuilder.toString();
    }

    private String buildTransactedQuery(UserSegmentQuery userSegmentQuery) throws JsonProcessingException, ParseException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select fon.order_customerid as ");
        queryBuilder.append(Constants.Alias.CUSTOMER_ID);
        if (userSegmentQuery.getNoOfOrders() > 0) {
            queryBuilder.append(Constants.Symbols.COMMA).append(System.lineSeparator());
            queryBuilder.append(" count(distinct fon.nykaa_orderno) as ").append(Constants.Alias.TOTAL_ORDER);
            
        }
        if (StringUtils.isNotBlank(userSegmentQuery.getAvgSpendStartDate()) || StringUtils.isNotBlank(userSegmentQuery.getAvgSpendEndDate())) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date avgSpendStartDate = formatter.parse(userSegmentQuery.getAvgSpendStartDate());
            Date avgSpendEndDate = formatter.parse(userSegmentQuery.getAvgSpendEndDate());
            long differenceOfDays = TimeUnit.DAYS.convert(Math.abs(avgSpendEndDate.getTime() - avgSpendStartDate.getTime()), TimeUnit.MILLISECONDS) + 1;
            double noOfMonths = (double) differenceOfDays/30;
            noOfMonths = CommonUtils.roundOff(noOfMonths);
            queryBuilder.append(Constants.Symbols.COMMA).append(System.lineSeparator());
            queryBuilder.append(" (sum(case when ").append(System.lineSeparator());
            queryBuilder.append(" fon.Order_Date >= '").append(userSegmentQuery.getAvgSpendStartDate()).append(Constants.START_OF_THE_DAY).append(System.lineSeparator());
            queryBuilder.append(" and fon.Order_Date <= '").append(userSegmentQuery.getAvgSpendEndDate()).append(Constants.END_OF_THE_DAY).append(System.lineSeparator());
            queryBuilder.append(" then f.orderdetail_total_amount else 0 end)) ");
            if (noOfMonths > 0) {
                queryBuilder.append("/").append(noOfMonths);
            }
            queryBuilder.append(" as ").append(Constants.Alias.AVG_SPEND)
                    .append(System.lineSeparator());
        }
        queryBuilder.append(" FROM beauty.fact_order_detail_new f ").append(System.lineSeparator())
        .append(" JOIN beauty.fact_order_view fon ON f.nykaa_orderno = fon.nykaa_orderno ").append(System.lineSeparator())
//        Dim SKUs are not exactly mapped to product_sku
//        .append(" join beauty.dim_sku d on f.product_sku = d.sku ").append(System.lineSeparator())
        .append(" join beauty.nykaad_customer_info_view nciv on nciv.customer_id = fon.order_customerid ").append(System.lineSeparator())
        .append(" join beauty.address_info ad on ad.user_id = nciv.customer_id ").append(System.lineSeparator())
        .append(" where coalesce(fon.is_test_order,0) != 1 ").append(System.lineSeparator())
        .append(" and nciv.status = 3 ").append(System.lineSeparator())
        .append(" and coalesce(fon.eretail_orderno,'') != '' ").append(System.lineSeparator())
        .append(" and lower(fon.order_source) = 'nykaad' ").append(System.lineSeparator())
        .append(" and upper(fon.coupon_code) not like '%TESTQA%' ").append(System.lineSeparator())
        .append(" and upper(fon.order_addressline) not like '%TEST%' ").append(System.lineSeparator())
        .append(" and upper(fon.data_source_id) not in ('NYKAAD_ERETAIL','RPL_OMS_NYKAAD') ").append(System.lineSeparator())
        .append(" and coalesce(is_free_product,0) = 0 ").append(System.lineSeparator());
//        .append(" and (case when (trunc(fon.order_dt_cancelled) = trunc(fon.order_date)) then 'yes' else  'no' end ) = 'no' ").append(System.lineSeparator());
        if (StringUtils.isNotBlank(userSegmentQuery.getOrderPlacedStartDate())) {
            queryBuilder.append(" and fon.order_date >= '").append(userSegmentQuery.getOrderPlacedStartDate()).append("' ").append(System.lineSeparator());
        }
        if (StringUtils.isNotBlank(userSegmentQuery.getOrderPlacedEndDate())) {
            queryBuilder.append(" and fon.order_date <= '").append(userSegmentQuery.getOrderPlacedEndDate()).append("' ").append(System.lineSeparator());
        }
        if (CollectionUtils.isNotEmpty(userSegmentQuery.getCustomerGroups())) {
            queryBuilder.append(" and nciv.business_type in ").append(objectMapper.writeValueAsString(userSegmentQuery.getCustomerGroups())).append(System.lineSeparator());
        }
        if (CollectionUtils.isNotEmpty(userSegmentQuery.getClusters())) {
            List<String> pincodes = new ArrayList<>();
            for (String cluster : userSegmentQuery.getClusters()) {
                pincodes.addAll(pincodeClusterMap.get(cluster));
            }
            queryBuilder.append(" and ad.postcode in ").append(objectMapper.writeValueAsString(pincodes)).append(System.lineSeparator());
        }
        if (CollectionUtils.isNotEmpty(userSegmentQuery.getBrandNames())) {
            queryBuilder.append(" and d.brand_name in ").append(objectMapper.writeValueAsString(userSegmentQuery.getBrandNames())).append(System.lineSeparator());
        }
        queryBuilder.append(" group by 1 ").append(System.lineSeparator());
        if (userSegmentQuery.getNoOfOrders() > 0 || userSegmentQuery.getMinAvgSpend() > 0 || userSegmentQuery.getMaxAvgSpend() > 0) {
            queryBuilder.append(" having ");
            boolean isFirstCondition = true;
            if (userSegmentQuery.getNoOfOrders() > 0) {
                queryBuilder.append(Constants.Alias.TOTAL_ORDER).append(" = ").append(userSegmentQuery.getNoOfOrders()).append(System.lineSeparator());
            }
            if (userSegmentQuery.getMinAvgSpend() > 0) {
                queryBuilder.append(isFirstCondition ? StringUtils.EMPTY : " and ");
                queryBuilder.append(Constants.Alias.AVG_SPEND).append(" >= ").append(userSegmentQuery.getMinAvgSpend()).append(System.lineSeparator());
                isFirstCondition = false;
            }
            if (userSegmentQuery.getMaxAvgSpend() > 0) {
                queryBuilder.append(isFirstCondition ? StringUtils.EMPTY : " and ");
                queryBuilder.append(Constants.Alias.AVG_SPEND).append(" <= ").append(userSegmentQuery.getMaxAvgSpend()).append(System.lineSeparator());
                isFirstCondition = false;
            }
        }
        return cleanQuery(queryBuilder);
    }
    

    private String cleanQuery(StringBuilder queryBuilder) {
        queryBuilder.append(" ; ");
        String query = queryBuilder.toString();
        query = query.replace('[', '(');
        query = query.replace(']', ')');
        query = query.replace("\"", "'");
        return query;
    }

    @Async
    private void sendMailResponse(List<String> alreadyMappedCustomers, OfferRuleDetails loyaltyOffer) {
        // sample error msg to-do
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append(SystemPropertyUtil.getLoyaltyMappingFailedContent());
        errorMsg.append(System.lineSeparator());
        errorMsg.append("For Loyalty Offer - " + loyaltyOffer.getOfferRuleName());
        errorMsg.append(System.lineSeparator());
        errorMsg.append("Already mapped customers with similar offer are - ");
        errorMsg.append(alreadyMappedCustomers);
        StringBuilder emailRecipients = new StringBuilder(SystemPropertyUtil.getRecipientsMailIds());
        emailUtil.sendMailToAdmin(emailRecipients.toString(), Constants.EmailParams.FILE_NAME, errorMsg.toString(),
                EmailParams.LOYALTY_OFFER, Constants.EmailParams.LOYALTY_NOTIFICATION_TEMPLATE);

    }

    private UserLoyaltyDetailsDto createUserLoyaltyDetail(Long offerId, String offerTypeUserResult) {
        UserLoyaltyDetailsDto userLoyaltyDetailsDto = new UserLoyaltyDetailsDto();
        Optional<OfferRuleDetails> currentOfferOptional = offerRuleDetailsReadRepository.findByIdAndIsActive(offerId, true);
        if (currentOfferOptional.isPresent()) {
            OfferRuleDetails currentOffer = currentOfferOptional.get();
            userLoyaltyDetailsDto.setTargets(createOfferTargetValuesList(currentOffer.getTargetType(),
                    currentOffer, offerTypeUserResult));
            userLoyaltyDetailsDto.setRewards(createOfferRewardValuesList(userLoyaltyDetailsDto.getTargets(),
                    currentOffer.getRewardPointValues(), currentOffer.getRewardPointType()));
            userLoyaltyDetailsDto.setAppliedLoyaltyOfferDto(offerRuleDetailsMapper.entityToDto(currentOffer));
            return userLoyaltyDetailsDto;
        } else {
            log.info("Mapped offer : {} is either disabled or not exists with customer", offerId);
            // TO DO remove the mapping
        }
        return null;
    }

    private List<Long> createOfferRewardValuesList(List<Long> targets, List<String> rewardPointValues, 
                                                   RewardPointsType rewardPointType) {
        switch (rewardPointType) {
            case FLAT:
                return rewardPointValues.stream().map(Long::valueOf).collect(Collectors.toList());
            case PERCENT:
                List<Long> calculatedRewards = new ArrayList<>();
                for (int i = 0; i < targets.size(); i++) {
                    long tierWiseReward = Long.valueOf(rewardPointValues.get(i));
                    calculatedRewards.add(CommonUtils.roundToNearestLong((tierWiseReward * targets.get(i)) / 100));
                }
                return calculatedRewards;
        }
        return null;
    }

    private List<Long> createOfferTargetValuesList(TargetType targetType, OfferRuleDetails currentOffer,
                                                   String offerTypeUserResult) {
        switch (targetType) {
            case FIXED:
                return currentOffer.getTargetValues().stream().map(Long::valueOf).collect(Collectors.toList());
            case VARIABLE:
                double userResult = Double.valueOf(StringUtils.isNotBlank(offerTypeUserResult) ? offerTypeUserResult : "0");
                List<Long> calculatedTargets = new ArrayList<>();
                if (currentOffer.getOfferType().equals(OfferType.TARGET_SPEND)) {
                    currentOffer.getTargetValues().stream().forEach(target -> {
                        double tierWiseTarget = Double.valueOf(target);
                        calculatedTargets.add(CommonUtils.roundToNearest100thCeiling(tierWiseTarget * userResult));
                    });
                } else {
                    currentOffer.getTargetValues().stream().forEach(target -> {
                        double tierWiseTarget = Double.valueOf(target);
                        calculatedTargets.add(CommonUtils.roundToNearestLong(tierWiseTarget * userResult));
                    });
                }
                return calculatedTargets;
        }
        return null;
    }

    @Override
    public void updateCurrentProgressForOffers(OrderDetails order) {
        List<LoyaltyCustomerOfferMapping> activeOffers = loyaltyCustomerOfferMappingReadRepository.findByCustomerIdAndRunningAndStatusNotCredited(order.getUserId(), order.getCreated());
        for (LoyaltyCustomerOfferMapping offerMapping : activeOffers) {
            OfferRuleDetails offerDetails = offerRuleDetailsReadRepository.findById(offerMapping.getOfferId()).get();
            switch (offerDetails.getOfferType()) {
            case TARGET_SPEND:
                offerMapping.setCurrentProgress(
                        String.valueOf(Float.parseFloat(offerMapping.getCurrentProgress()) + order.getTotalAmount()));
                break;
            case NO_OF_ORDERS:
                offerMapping
                        .setCurrentProgress(String.valueOf(Integer.parseInt(offerMapping.getCurrentProgress()) + 1));
                break;
            default:
                break;
            }
            loyaltyCustomerOfferMappingRepository.save(offerMapping);
            if (DateUtil.getDateAfterDays(0).after(offerDetails.getEndDate()) && !isActiveOrderPresent(offerMapping, offerDetails)) {
                log.info(
                        "last active order's return period is over with status {}. crediting reward points for user id {} and offer id {}",
                        order.getOrderStatus(), offerMapping.getCustomerId(), offerMapping.getOfferId());
                creditRewardPoints(offerMapping, offerDetails);
            }
        }
    }

    @Override
    public void settleLifecycleCompleteOrder(OrderDetails order) {
        List<LoyaltyCustomerOfferMapping> activeOffers = loyaltyCustomerOfferMappingReadRepository.findByCustomerIdAndRunningAndStatusNotCredited(order.getUserId(), order.getCreated());
        for (LoyaltyCustomerOfferMapping offerMapping : activeOffers) {
            OfferRuleDetails offerDetails = offerRuleDetailsReadRepository.findById(offerMapping.getOfferId()).get();
            if (DateUtil.getDateAfterDays(0).after(offerDetails.getEndDate()) && !isLastActiveOrderPresent(offerMapping, offerDetails, order)) {
                log.info(
                        "last active order's return period is over with status {}. crediting reward points for user id {} and offer id {}",
                        order.getOrderStatus(), offerMapping.getCustomerId(), offerMapping.getOfferId());
                creditRewardPoints(offerMapping, offerDetails);
            }
        }
    }

    private boolean isLastActiveOrderPresent(LoyaltyCustomerOfferMapping offerMapping, OfferRuleDetails offerDetails, OrderDetails order) {
        return orderDetailsReadRepository.existsByUserIdAndOrderStatusAndCreatedBetweenAndOrderIdNot(offerMapping.getCustomerId(),
                OrderStatus.PROCESSING, offerDetails.getStartDate(), offerDetails.getEndDate(), order.getOrderId());
    }

    private boolean isActiveOrderPresent(LoyaltyCustomerOfferMapping offerMapping, OfferRuleDetails offerDetails) {
        return orderDetailsReadRepository.existsByUserIdAndOrderStatusAndCreatedBetween(offerMapping.getCustomerId(),
                OrderStatus.PROCESSING, offerDetails.getStartDate(), offerDetails.getEndDate());
    }

    @Override
    @Async
    public void endLoyaltyOffer(LoyaltyEventRequestDto requestDto) {
        OfferRuleDetails offerRule = offerRuleDetailsReadRepository.findById(requestDto.getOfferId()).get();
        List<LoyaltyCustomerOfferMapping> offerMappings = loyaltyCustomerOfferMappingReadRepository.findByOfferId(offerRule.getId());
        for (LoyaltyCustomerOfferMapping offerMapping : offerMappings) {
            if (isActiveOrderPresent(offerMapping, offerRule)) {
                log.info("Active order(s) are present for user id {} and offer id {}", offerMapping.getCustomerId(), offerMapping.getOfferId());
                continue;
            }
            creditRewardPoints(offerMapping, offerRule);
        }
        offerRule.setIsActive(false);
        offerRuleDetailsRepository.save(offerRule);
    }

    private void creditRewardPoints(LoyaltyCustomerOfferMapping offerMapping, OfferRuleDetails offerRule) {
        Double rewardPoints = calculateRewardPoints(offerMapping, offerRule, Double.parseDouble(offerMapping.getCurrentProgress())).getRight();
        if (rewardPoints <= 0.0) {
            log.info("No reward points to be credited to user id {} for offer id {}", offerMapping.getCustomerId(), offerMapping.getOfferId());
            return;
        }
        try {
            CreateTransactionDTO transaction = createTransactionDto(offerMapping, offerRule, rewardPoints);
            if (SystemPropertyUtil.isVaultEnabled()) {
                ResponseBean<TransactionDTO> response = vaultClientService.performTransaction(transaction);
                if (!response.isSuccess()) {
                    log.error(
                            "Error response received from vault while crediting reward points for customer id {} and offer id {}",
                            offerMapping.getCustomerId(), offerMapping.getOfferId());
                }
                offerMapping.setCreditStatus(response.isSuccess() ? RewardPointsCreditStatus.CREDITED : RewardPointsCreditStatus.FAILED);
                offerMapping.setCreditedRewardPoints(rewardPoints.floatValue());
            }
        } catch (NykaaWebServiceException e) {
            offerMapping.setCreditStatus(RewardPointsCreditStatus.FAILED);
            log.error("Exception occured while calling vault api for customer id {} and offer id {}: {}",
                    offerMapping.getCustomerId(), offerMapping.getOfferId(), e.getMessage());
        } finally {
            loyaltyCustomerOfferMappingRepository.save(offerMapping);
        }
    }

    private CreateTransactionDTO createTransactionDto(LoyaltyCustomerOfferMapping offerMapping, OfferRuleDetails offerRule, Double rewardPoints) {
        CreateTransactionDTO transaction = new CreateTransactionDTO();
        transaction.setReferenceId(offerRule.getOfferRuleName());
        StringBuilder externalReferenceId = new StringBuilder();
        externalReferenceId.append(Constants.LOYALTY).append(offerMapping.getCustomerId()).append(Constants.Symbols.UNDERSCORE)
                .append(offerMapping.getOfferId());
        transaction.setExternalReferenceId(externalReferenceId.toString());
        transaction.setCustomerId(Long.valueOf(offerMapping.getCustomerId()));
        transaction.setAmount(rewardPoints);
        transaction.setDeviceType(Constants.DEFAULT);
        transaction.setTransactionType(TransactionType.CREDIT_NYKAA_REWARDS);
        transaction.setVaultType(VaultType.PROMOTION);
        transaction.setVaultSubType(Constants.SUPERSTORE_LOYALTY);
        transaction.setAddedBy(Constants.SYSTEM);
        transaction.setDomain(Domain.NYKAA_D.getName());
        transaction.setNykaaFundedAmount(rewardPoints * offerRule.getNykaaShare()/100);
        transaction.setBrandFundedAmount(rewardPoints * offerRule.getBrandShare()/100);
        int expiryDays = getExpiryDays(offerRule.getRewardPointExpiryValue(), offerRule.getRewardPointExpiryUnit());
        transaction.setExpiryDays(expiryDays);
        return transaction;
    }

    private int getExpiryDays(Integer rewardPointExpiryValue, RewardPointsExpiryUnit rewardPointExpiryUnit) {
        int days = 0;
        switch (rewardPointExpiryUnit) {
        case DAYS:
            days = rewardPointExpiryValue;
            break;
        case MONTHS:
            days = rewardPointExpiryValue * 30;
        case YEARS:
            days = rewardPointExpiryValue * 365;
        default:
            break;
        }
        return days;
    }

    private ImmutablePair<Integer, Double> calculateRewardPoints(LoyaltyCustomerOfferMapping offerMapping, OfferRuleDetails offerRule, Double progress) {
        int rewardIndex = 0;
        Double rewardPoints = 0.0;
        int size = CommonUtils.getNoOfConfiguredTiers(offerRule.getTargetValues());
        for (int i = 1; i <= size; i++) {
            if (NumberUtils.isParsable(offerRule.getTargetValues().get(i - 1))) {
                Double targetValue = offerRule.getTargetType().equals(TargetType.FIXED)
                        ? Double.parseDouble(offerRule.getTargetValues().get(i - 1))
                        : CommonUtils.roundToNearest100thCeiling(
                                Double.parseDouble(offerRule.getTargetValues().get(i - 1)) * Double
                                        .parseDouble(StringUtils.isNotBlank(offerMapping.getOfferTypeUserResult())
                                                ? offerMapping.getOfferTypeUserResult()
                                                : "0"));
                if (progress < targetValue) {
                    rewardIndex = i - 1;
                    break;
                }
            }
            if (i == size) {
                rewardIndex = size;
            }
        }
        if (rewardIndex > 0) {
            rewardPoints = offerRule.getRewardPointType().equals(RewardPointsType.FLAT)
                    ? Double.parseDouble(offerRule.getRewardPointValues().get(rewardIndex - 1))
                    : ((Double.parseDouble(offerRule.getRewardPointValues().get(rewardIndex - 1)) * progress)/100);
            rewardPoints = rewardPoints > offerRule.getMaxReward() ? offerRule.getMaxReward() : rewardPoints;
        }
        return new ImmutablePair<>(rewardIndex, rewardPoints);
    }

    @Override
    public void checkAndProcessOfferRewards(OrderDetails order) {
        List<LoyaltyCustomerOfferMapping> activeOffers = loyaltyCustomerOfferMappingReadRepository.findByCustomerIdAndRunningAndStatusNotCredited(order.getUserId(), order.getCreated());
        for (LoyaltyCustomerOfferMapping offerMapping : activeOffers) {
            OfferRuleDetails offerDetails = offerRuleDetailsReadRepository.findById(offerMapping.getOfferId()).get();
            if (DateUtil.getDateAfterDays(0).after(offerDetails.getEndDate())
                    && DateUtil.getDateAfterDays(0).after(order.getReturnPeriodOverDate())
                    && offerMapping.getCreditStatus().equals(RewardPointsCreditStatus.PENDING)
                    && !isActiveOrderPresent(offerMapping, offerDetails)) {
                log.info(
                        "last active order's journey is complete with status {}. crediting reward points for user id {} and offer id {}",
                        order.getOrderStatus(), offerMapping.getCustomerId(), offerMapping.getOfferId());
                creditRewardPoints(offerMapping, offerDetails);
            }
        }
    }

    @Override
    @Async
    public void retryFailedTransaction() {
        List<LoyaltyCustomerOfferMapping> failedTransactions = loyaltyCustomerOfferMappingReadRepository.findByCreditStatus(RewardPointsCreditStatus.FAILED);
        for (LoyaltyCustomerOfferMapping transaction : failedTransactions) {
            OfferRuleDetails offerRule = offerRuleDetailsReadRepository.findById(transaction.getOfferId()).get();
            creditRewardPoints(transaction, offerRule);
        }
    }


    private LoyaltyCustomerOfferMapping createOfferMappingEntryForNewUser(OfferRuleDetails newUserOffer, String customerId) {
        LoyaltyCustomerOfferMapping newEntry = new LoyaltyCustomerOfferMapping();
        newEntry.setOfferId(newUserOffer.getId());
        newEntry.setEndDate(newUserOffer.getEndDate());
        newEntry.setStartDate(newUserOffer.getStartDate());
        newEntry.setCustomerId(customerId);
        newEntry.setCreditStatus(RewardPointsCreditStatus.PENDING);
        newEntry.setCurrentProgress(setCurrentProgressByType(newUserOffer.getOfferType()));
        return newEntry;
    }

    private void validateRequest(NewUserEventDTO newUserEventDTO) {
        if (StringUtils.isBlank(newUserEventDTO.getDomain())
                || !Constants.SUPERSTORE_DOMAIN.equalsIgnoreCase(newUserEventDTO.getDomain())) {
            log.error("Invalid domain : {}", newUserEventDTO.getDomain());
            throw new LoyaltyException(ErrorCodes.INVALID_DOMAIN);
        }
    }

    @Override
    public void adjustUserPotential(OrderDetails order, float adjustmentAmount, int factor) {
        List<LoyaltyCustomerOfferMapping> activeOffers = loyaltyCustomerOfferMappingReadRepository.findByCustomerIdAndRunning(order.getUserId(), order.getCreated());
        log.info("found {} offers for adjusting user potential");
        for (LoyaltyCustomerOfferMapping offerMapping : activeOffers) {
            OfferRuleDetails offerDetails = offerRuleDetailsReadRepository.findById(offerMapping.getOfferId()).get();
            switch (offerDetails.getOfferType()) {
            case TARGET_SPEND:
                offerMapping.setPotentialProgress((Objects.nonNull(offerMapping.getPotentialProgress())
                        ? offerMapping.getPotentialProgress()
                        : 0) + (adjustmentAmount * factor));
                break;
            case NO_OF_ORDERS:
                offerMapping.setPotentialProgress((float) ((Objects.nonNull(offerMapping.getPotentialProgress())
                        ? offerMapping.getPotentialProgress().intValue()
                        : 0) + (1 * factor)));
                break;
            default:
                break;
            }
            updateUserPotential(offerDetails, offerMapping);
        }
    }

    private void updateUserPotential(OfferRuleDetails offerDetails, LoyaltyCustomerOfferMapping offerMapping) {
        ImmutablePair<Integer, Double> rewardsDetails = calculateRewardPoints(offerMapping, offerDetails, (double) offerMapping.getPotentialProgress());
        offerMapping.setCurrentTier(rewardsDetails.getLeft());
        offerMapping.setPotentialEarning(rewardsDetails.getRight().floatValue());
        loyaltyCustomerOfferMappingRepository.save(offerMapping);
    }

    @Override
    public UserOfferResponseDto getUserLoyaltyOffers(String customerId) {
        UserOfferResponseDto response = new UserOfferResponseDto();
        List<LoyaltyCustomerOfferMapping> currentOfferMappings = loyaltyCustomerOfferMappingReadRepository
                .findByCustomerIdAndOfferDurationInRange(customerId, DateUtil.getStartDateOfMonth(0),
                        DateUtil.getEndDateOfMonth(0));
        double totalPotentialEarning = 0;
        if (CollectionUtils.isNotEmpty(currentOfferMappings)) {
            for (LoyaltyCustomerOfferMapping offerMapping : currentOfferMappings) {
                totalPotentialEarning += offerMapping.getPotentialEarning() != null ?
                      offerMapping.getPotentialEarning().floatValue() : 0;
                response.getActiveOffers().add(buildActiveOfferDetails(offerMapping));
            }
        } else {
            response.setNoActiveOffersMessage(SystemPropertyUtil.getNoActiveOfferMessage());
        }
        response.setTotalPotentialEarning(CommonUtils.roundOff(totalPotentialEarning));
        response.setPastEarning(buildUserPastEarning(customerId));
        return response;
    }

    private List<UserEarningDetails> buildUserPastEarning(String customerId) {
        List<UserEarningDetails> pastEarningDetails = new ArrayList<>();
        for (int i = 0; i < SystemPropertyUtil.getPastEarningMonths(); i++) {
            pastEarningDetails.add(getUserPastEarningDetails(-1 * (i+1), customerId));
        }
        return pastEarningDetails;
    }

    private UserEarningDetails getUserPastEarningDetails(int month, String customerId) {
        List<LoyaltyCustomerOfferMapping> pastOfferMappings = loyaltyCustomerOfferMappingReadRepository
                .findByCustomerIdAndOfferDurationInRange(customerId, DateUtil.getStartDateOfMonth(month),
                        DateUtil.getEndDateOfMonth(month));
        UserEarningDetails earningDetails = new UserEarningDetails();
        boolean isPending = false;
        for (LoyaltyCustomerOfferMapping offerMapping : pastOfferMappings) {
            if (Objects.isNull(offerMapping.getCreditedRewardPoints())) {
                offerMapping.setCreditedRewardPoints(0.0f);
            }
            if (offerMapping.getCreditStatus().equals(RewardPointsCreditStatus.PENDING)) {
                isPending = true;
            }
        }
        Calendar calendar = DateUtil.getCalendarByMonth(month);
        StringBuilder builder = new StringBuilder();
        builder.append(calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())).append(Constants.Symbols.SPACE)
        .append(calendar.get(Calendar.YEAR));
        earningDetails.setMonth(builder.toString());
        earningDetails
                .setStatus((isPending || CollectionUtils.isEmpty(pastOfferMappings) ? RewardPointsCreditStatus.PENDING
                        : RewardPointsCreditStatus.CREDITED).name());
        earningDetails.setEarning(pastOfferMappings.stream().mapToDouble(LoyaltyCustomerOfferMapping::getCreditedRewardPoints).sum());
        return earningDetails;
    }

    private UserOfferDetails buildActiveOfferDetails(LoyaltyCustomerOfferMapping offerMapping) {
        UserOfferDetails offerDetails = new UserOfferDetails();
        offerDetails.setOfferId(offerMapping.getOfferId());
        offerDetails.setPotentialProgress(offerMapping.getPotentialProgress() != null ?
                offerMapping.getPotentialProgress().doubleValue() : 0.0);
        offerDetails.setPotentialSupercashEarning(CommonUtils.roundOff(
                offerMapping.getPotentialEarning() != null ? offerMapping.getPotentialEarning() : 0.0));
        offerDetails.setTierPassed(offerMapping.getCurrentTier());
        Optional<OfferRuleDetails> offerRuleDetailsOptional = offerRuleDetailsReadRepository
                .findByIdAndIsActive(offerMapping.getOfferId(), true);
        if (offerRuleDetailsOptional.isPresent()) {
            setOfferRuleDetails(offerRuleDetailsOptional.get(), offerDetails, offerMapping);
        }
        return offerDetails;
    }

    private void setOfferRuleDetails(OfferRuleDetails offerRuleDetails, UserOfferDetails offerDetails,
            LoyaltyCustomerOfferMapping offerMapping) {
        offerDetails.setOfferName(offerRuleDetails.getOfferRuleName());
        offerDetails.setMaxRewardLimit(offerRuleDetails.getMaxReward());
        offerDetails.setTiers(buildUserTiers(offerMapping, offerRuleDetails));
        offerDetails.setNextOfferMessage(buildNextOfferMessage(offerDetails, offerMapping, offerRuleDetails));
        offerDetails.setOfferStartDate(DateUtil.parseDate(offerRuleDetails.getStartDate()));
        offerDetails.setOfferEndDate(DateUtil.parseDate(offerRuleDetails.getEndDate()));
        offerDetails.setOfferTitle(
                StringUtils.isNotBlank(offerRuleDetails.getOfferTitle()) ? offerRuleDetails.getOfferTitle()
                        : buildOfferTitle(offerRuleDetails, offerMapping));
        offerDetails.setOfferSubtitle(
                StringUtils.isNotBlank(offerRuleDetails.getOfferSubtitle()) ? offerRuleDetails.getOfferSubtitle()
                        : buildOfferSubtitle(offerRuleDetails, offerMapping));
    }

    private String buildOfferSubtitle(OfferRuleDetails offerRuleDetails, LoyaltyCustomerOfferMapping offerMapping) {
        Map<String, String> offerTitleTemplateMap = SystemPropertyUtil
                .getOfferTitleMap(offerRuleDetails.getOfferType().name());
        return offerTitleTemplateMap.get(offerMapping.getCurrentTier() == 0 ? Constants.ZERO_TIER_OFFER_SUBTITLE
                : Constants.NEXT_TIER_OFFER_SUBTITLE);
    }

    private String buildOfferTitle(OfferRuleDetails offerRuleDetails, LoyaltyCustomerOfferMapping offerMapping) {
        int size = CommonUtils.getNoOfConfiguredTiers(offerRuleDetails.getRewardPointValues());
        Map<String, String> offerTitleTemplateMap = SystemPropertyUtil
                .getOfferTitleMap(offerRuleDetails.getOfferType().name());
        String template = offerTitleTemplateMap.get(
                offerMapping.getCurrentTier() == 0 ? Constants.ZERO_TIER_OFFER_TITLE : Constants.NEXT_TIER_OFFER_TITLE);
        StringBuilder rewardBuilder = new StringBuilder();
        rewardBuilder
                .append(offerMapping.getCurrentTier() == 0 ? offerRuleDetails.getRewardPointValues().get(size - 1)
                        : offerRuleDetails.getRewardPointValues().get(offerMapping.getCurrentTier() - 1))
                .append(offerRuleDetails.getRewardPointType().equals(RewardPointsType.PERCENT)
                        ? Constants.Symbols.PERCENT
                        : StringUtils.EMPTY);

        return template.replace(offerMapping.getCurrentTier() == 0 ? Constants.MAX_REWARD : Constants.TIER_REWARD,
                rewardBuilder.toString());
    }

    private String buildNextOfferMessage(UserOfferDetails offerDetails, LoyaltyCustomerOfferMapping offerMapping,
            OfferRuleDetails offerRuleDetails) {
        Map<String, String> templateMap = SystemPropertyUtil
                .getNextMessageTemplateMap(offerRuleDetails.getOfferType().name());
        int size = CommonUtils.getNoOfConfiguredTiers(offerRuleDetails.getTargetValues());
        if (offerDetails.getTierPassed() == size) {
            return templateMap.get(Constants.MAX_TIER_MESSAGE_TEMPLATE);
        }
        String deltaSpend = String.valueOf((offerRuleDetails.getTargetType().equals(TargetType.FIXED)
                ? Double.parseDouble(offerRuleDetails.getTargetValues().get(offerDetails.getTierPassed()))
                : CommonUtils.roundToNearest100thCeiling(
                        Double.parseDouble(offerRuleDetails.getTargetValues().get(offerDetails.getTierPassed()))
                                * Double.parseDouble(StringUtils.isNotBlank(offerMapping.getOfferTypeUserResult())
                                        ? offerMapping.getOfferTypeUserResult()
                                        : "0")))
                - offerDetails.getPotentialProgress());
        deltaSpend = CommonUtils.formatDouble(Double.valueOf(deltaSpend));
        StringBuilder nextRewardBuilder = new StringBuilder();
        nextRewardBuilder.append(offerRuleDetails.getRewardPointValues().get(offerDetails.getTierPassed()))
                .append(offerRuleDetails.getRewardPointType().equals(RewardPointsType.PERCENT)
                        ? Constants.Symbols.PERCENT
                        : StringUtils.EMPTY);

        return templateMap.get(Constants.NEXT_TIER_MESSAGE_TEMPLATE)
                          .replace(Constants.DELTA_SPEND, deltaSpend)
                          .replace(Constants.NEXT_REWARD, nextRewardBuilder.toString())
                          .replace(Constants.DOUBLE_DECIMAL, Constants.Symbols.SPACE);
    }

    private List<TierDetails> buildUserTiers(LoyaltyCustomerOfferMapping offerMapping,
            OfferRuleDetails offerRuleDetails) {
        List<TierDetails> tierDetails = new ArrayList<>();
        for (int i = 0; i < offerRuleDetails.getRewardPointValues().size(); i++) {
            if (StringUtils.isNotBlank(offerRuleDetails.getTargetValues().get(i))) {
                TierDetails tier = new TierDetails();
                tier.setTarget(offerRuleDetails.getTargetType().equals(TargetType.FIXED)
                        ? Double.valueOf(offerRuleDetails.getTargetValues().get(i))
                        : CommonUtils
                                .roundToNearest100thCeiling(Double.valueOf(offerRuleDetails.getTargetValues().get(i))
                                        * Double.valueOf(StringUtils.isNotBlank(offerMapping.getOfferTypeUserResult())
                                                ? offerMapping.getOfferTypeUserResult()
                                                : "0")));
                tier.setReward(Double.valueOf(offerRuleDetails.getRewardPointValues().get(i)));
                tier.setRewardType(offerRuleDetails.getRewardPointType().name());
                tierDetails.add(tier);
            }
        }
        return tierDetails;
    }

    private void retryLoyaltyStartEvent(LoyaltyEventRequestDto loyaltyEventRequestDto) {
        loyaltyEventRequestDto.setCounter(loyaltyEventRequestDto.getCounter() + 1);
        loyaltyEventRequestDto.setTriggerFireTime(DateUtils.addMinutes(new Date(), Integer.valueOf(SystemPropertyUtil
                .getProperty(Constants.Dwh.RETRY_QUERY_EXECUTION_TIME, Constants.Dwh.DEFAULT_RETRY_QUERY_EXECUTION_TIME))));
        eventSchedulerService.scheduleLoyaltyOfferEvent(loyaltyEventRequestDto);
    }

    @Override
    public void mapCustomerToOffer(CustomerOfferMappingDto requestDto) {
        Optional<OfferRuleDetails> offerRuleDetails = offerRuleDetailsReadRepository
                .findByIdAndIsActive(requestDto.getOfferId(), true);
        if (!offerRuleDetails.isPresent()) {
            log.error("Offer rule cannot be found");
            throw new LoyaltyException(ErrorCodes.OFFER_RULE_NOT_FOUND);
        }
        OfferRuleDetails loyaltyOffer = offerRuleDetails.get();
        Date currentDate = new Date();
        if (currentDate.after(loyaltyOffer.getEndDate())) {
            log.error("Offer end date surpassed current date");
            throw new LoyaltyException(ErrorCodes.OFFER_END_DATE_SURPASSED);
        }
        if (!checkForExistingMapping(requestDto.getCustomerId(), loyaltyOffer)) {
            log.error("existing mapping found for customer id {} and offer id {}", requestDto.getCustomerId(),
                    requestDto.getOfferId());
            throw new LoyaltyException(ErrorCodes.OFFER_CUSTOMER_MAPPING_ERROR);
        }
        log.info("creating customer offer mapping dto");
        LoyaltyCustomerOfferMapping customerOfferMapping = createOfferMappingEntry(loyaltyOffer,
                requestDto.getCustomerId(),
                CustomerType.TRANSACTED.equals(loyaltyOffer.getCustomerType()) ? requestDto.getPreviousCycleResult()
                        : null);
        log.info("customer offer mapping dto created");
        loyaltyCustomerOfferMappingRepository.save(customerOfferMapping);
        log.info("customer id {} mapped successfully to offer id {}", requestDto.getCustomerId(),
                requestDto.getOfferId());
    }

    @Override
    public List<Map<String, String>> executeQuery(QueryOfferMappingDto requestDto) {
        log.info("Executing query : {}", requestDto.getQuery());
        ResultSet resultSet;
        try {
            resultSet = queryHelper.executeQueryAndReturnResult(requestDto.getQuery());
        } catch (LoyaltyException ex) {
            if (ErrorCodes.REDSHIFT_QUERY_TIMEOUT.equals(ex.getErrorCode())) {
                log.info("Redshift query timed out retrying");
                return null;
            }
            throw ex;
        } catch (Exception e) {
            throw new LoyaltyException(ErrorCodes.REDSHIFT_CONNECTION_ERROR);
        }
        log.info("successfully executed requested query {}", requestDto.getQuery());
        if (Objects.isNull(resultSet)) {
            log.info("Empty Result Set obtained for query {}", requestDto.getQuery());
            return null;
        }
        List<Map<String, String>> result = new ArrayList<>();
        try {
            while (resultSet.next()) {
                try {
                    Map<String, String> resultMap = new HashMap<>();
                    for (String column : requestDto.getColumns()) {
                        resultMap.put(column, String.valueOf(resultSet.getObject(column)));
                    }
                    result.add(resultMap);
                } catch (Exception e) {
                    log.info("Error processing row in the result set");
                }
            }
        } catch (SQLException e) {
            log.info("Error processing the result set");
        }
        return result;
    }

    @Override
    public LoyaltyCustomerOfferMapping updateCustomerOfferMapping(LoyaltyCustomerOfferMapping requestDto) {
        Optional<LoyaltyCustomerOfferMapping> customerMappingOptional = loyaltyCustomerOfferMappingReadRepository
                .findByCustomerIdAndOfferId(requestDto.getCustomerId(), requestDto.getOfferId());
        if (!customerMappingOptional.isPresent()) {
            log.error("Customer Offer Mapping not found for customer id {} and offer id {}", requestDto.getCustomerId(),
                    requestDto.getOfferId());
            throw new LoyaltyException(ErrorCodes.OFFER_CUSTOMER_MAPPING_ERROR);
        }
        LoyaltyCustomerOfferMapping customerMapping = customerMappingOptional.get();
        customerMapping.setCreditedRewardPoints(requestDto.getCreditedRewardPoints());
        customerMapping.setCreditStatus(requestDto.getCreditStatus());
        customerMapping.setCurrentProgress(requestDto.getCurrentProgress());
        customerMapping.setCurrentTier(requestDto.getCurrentTier());
        customerMapping.setOfferTypeUserResult(requestDto.getOfferTypeUserResult());
        customerMapping.setPotentialEarning(requestDto.getPotentialEarning());
        customerMapping.setPotentialProgress(requestDto.getPotentialProgress());
        customerMapping = loyaltyCustomerOfferMappingRepository.save(customerMapping);
        log.info("customer offer mapping updated successfully for customer id {} and offer id {}",
                requestDto.getCustomerId(), requestDto.getOfferId());
        return customerMapping;
    }

    @Override
    public void mapLoyaltyOffers() throws Exception {
        Date currentDate = new Date();
        List<OfferRuleDetails> notMappedOffers = offerRuleDetailsReadRepository
                .findByMappingDoneAndIsActiveAndStartDateLessThanAndEndDateGreaterThan(false, true, currentDate, currentDate);
        if (CollectionUtils.isNotEmpty(notMappedOffers)) {
            for (OfferRuleDetails offer : notMappedOffers) {
                log.info("Executing customer target query for offer_rule_id : {}, query : {}", offer.getId(),
                        offer.getQuery());
                ResultSet resultSet = queryHelper.executeQueryAndReturnResult(offer.getQuery());
                log.info("query executed successfully for offer_rule_id {}", offer.getId());
                List<String> alreadyMappedCustomers = new ArrayList<>();
                List<LoyaltyCustomerOfferMapping> newMappings = new ArrayList<>();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        String customerId = String.valueOf(resultSet.getLong(Constants.Alias.CUSTOMER_ID));
                        if (!checkForExistingMapping(customerId, offer)) {
                            log.info("existing mapping found for customer id {} and offer id {}", customerId, offer.getId());
                            alreadyMappedCustomers.add(customerId);
                        } else {
                            String userPrevOfferTypeResult = CustomerType.TRANSACTED.equals(offer.getCustomerType()) ?
                                    getPrevCycleSpendValueByType(offer.getOfferType(), resultSet) : null;
                            newMappings.add(createOfferMappingEntry(offer, customerId, userPrevOfferTypeResult));
                        }
                    }
                }
                log.info("attaching offer {} to {} customers", offer.getOfferRuleName(), newMappings.size());
                offer.setMappingDone(Boolean.TRUE);
                offerRuleDetailsRepository.save(offer);
                loyaltyCustomerOfferMappingRepository.saveAll(newMappings);
                if (CollectionUtils.isNotEmpty(alreadyMappedCustomers)) {
                    log.info("{} already mapped customers are found for offer {}, sending mail to inform.", alreadyMappedCustomers.size(),
                            offer.getOfferRuleName());
                    sendMailResponse(alreadyMappedCustomers, offer);
                }
            }
        }
    }

}
