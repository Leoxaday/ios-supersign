package com.naoh.iossupersign.thridparty.appleapi;

import com.naoh.iossupersign.enums.AppleApiEnum;
import com.naoh.iossupersign.enums.ServiceError;
import com.naoh.iossupersign.exception.ServiceException;
import com.naoh.iossupersign.model.bo.AuthorizeBO;
import com.naoh.iossupersign.model.dto.AppleApiResult;
import com.naoh.iossupersign.model.dto.AppleReqBody;
import com.naoh.iossupersign.model.dto.AppleResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AppleApiService extends AppleApi{

    @Autowired
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * 取得帳號下的設備資訊
     * @param authorizeBO
     * @return
     */
    public List<AppleResultDTO> getNumberOfAvailableDevices(AuthorizeBO authorizeBO) {
        HttpHeaders headers = getToken(authorizeBO.getP8(), authorizeBO.getIss(), authorizeBO.getKid());
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        String url = AppleApiEnum.LIST_DEVICE_API.getApiPath();
        ResponseEntity<AppleApiResult<List<AppleResultDTO>>> response = restTemplate.exchange(url,AppleApiEnum.LIST_DEVICE_API.getHttpMethod(),httpEntity,
                new ParameterizedTypeReference<AppleApiResult<List<AppleResultDTO>>>(){});
        AppleApiResult<List<AppleResultDTO>> responseBody = response.getBody();
        return responseBody.getData();
    }

    /**
     * 註冊新設備到蘋果帳號
     * @param udid
     * @param authorizeBO
     * @return
     */
    public AppleResultDTO registerNewDevice(String udid , AuthorizeBO authorizeBO){
        HttpHeaders headers = getToken(authorizeBO.getP8(), authorizeBO.getIss(), authorizeBO.getKid());

        AppleReqBody attributes = AppleReqBody.init().add("name", udid).add("udid", udid).add("platform", "IOS");
        AppleReqBody body = AppleReqBody.init().add("type", "devices").add("attributes", attributes);
        AppleReqBody data = AppleReqBody.init().add("data",body);
        HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<>(data,headers);
        String url = AppleApiEnum.LIST_DEVICE_API.getApiPath();
        ResponseEntity<AppleApiResult<AppleResultDTO>> response = restTemplate.exchange(url,AppleApiEnum.REGISTER_NEW_DEVICE_API.getHttpMethod(),httpEntity,
                new ParameterizedTypeReference<AppleApiResult<AppleResultDTO>>(){});
        return response.getBody().getData();
    }

    /**
     * 註冊新BundleId
     * @param authorizeBO
     * @return
     */
    public AppleResultDTO registerNewBundleId(AuthorizeBO authorizeBO){
        try{
            HttpHeaders headers = getToken(authorizeBO.getP8(), authorizeBO.getIss(), authorizeBO.getKid());
            AppleReqBody attributes = AppleReqBody.init().add("identifier", "com.*").add("name", UUID.randomUUID().toString()).add("platform", "IOS");
            AppleReqBody body = AppleReqBody.init().add("type", "bundleIds").add("attributes", attributes);
            AppleReqBody data = AppleReqBody.init().add("data",body);
            HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<>(data,headers);
            String url = AppleApiEnum.REGISTER_NEW_BUNDLEID_API.getApiPath();
            ResponseEntity<AppleApiResult<AppleResultDTO>> response = restTemplate.exchange(url,AppleApiEnum.REGISTER_NEW_BUNDLEID_API.getHttpMethod(),httpEntity,
                    new ParameterizedTypeReference<AppleApiResult<AppleResultDTO>>(){});
            return response.getBody().getData();
        }catch (Exception e){
            throw new ServiceException(ServiceError.CREATE_BUNDLE_ERROR);
        }
    }

    /**
     * 產生profile檔
     * @param authorizeBO
     * @param bundleId
     * @param cerId
     * @param devId
     * @return
     */
    public AppleResultDTO getMobileprovision(AuthorizeBO authorizeBO, String bundleId, String cerId, String devId, String fileName){

        HttpHeaders headers = getToken(authorizeBO.getP8(), authorizeBO.getIss(), authorizeBO.getKid());

        AppleReqBody attributes = AppleReqBody.init().add("name", fileName).add("profileType", "IOS_APP_DEVELOPMENT");
        AppleReqBody relationships = getProfileRelationships(bundleId, cerId, devId);
        AppleReqBody body = AppleReqBody.init().add("attributes", attributes).add("relationships", relationships).add("type","profiles");
        AppleReqBody data = AppleReqBody.init().add("data",body);


        HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<>(data,headers);
        String url = AppleApiEnum.CREATE_PROFILE_API.getApiPath();

        ResponseEntity<AppleApiResult<AppleResultDTO>> response = restTemplate.exchange(url,AppleApiEnum.CREATE_PROFILE_API.getHttpMethod(),httpEntity,
                new ParameterizedTypeReference<AppleApiResult<AppleResultDTO>>(){});
        System.out.println(response.getBody().getData());
        File file = null;
        return response.getBody().getData();
    }

    private AppleReqBody getProfileRelationships(String bundleId,String cerId, String devId){

        AppleReqBody bundle = AppleReqBody.init().add("id", bundleId).add("type", "bundleIds");
        AppleReqBody bundleIds = AppleReqBody.init().add("data", bundle);


        AppleReqBody certificate = AppleReqBody.init().add("id", cerId).add("type", "certificates");
        List<AppleReqBody> certificatesList = new ArrayList<>();
        certificatesList.add(certificate);
        AppleReqBody certificates = AppleReqBody.init().add("data", certificatesList);

        List<AppleReqBody> deviceList = new ArrayList<>();
        AppleReqBody device = AppleReqBody.init().add("id", devId).add("type", "devices");
        deviceList.add(device);
        AppleReqBody devices = AppleReqBody.init().add("data", deviceList);

        AppleReqBody relationships = AppleReqBody.init().add("bundleId", bundleIds).add("certificates", certificates)
                .add("devices", devices);
        return relationships;
    }

    /**
     * 建立CER憑證檔
     * @param authorizeBO
     * @return
     */
    public AppleResultDTO insertCertificates(AuthorizeBO authorizeBO){
        HttpHeaders headers = getToken(authorizeBO.getP8(), authorizeBO.getIss(), authorizeBO.getKid());
        AppleReqBody attributes = AppleReqBody.init().add("csrContent", authorizeBO.getCsr()).add("certificateType", "IOS_DEVELOPMENT");
        AppleReqBody body = AppleReqBody.init().add("type", "certificates").add("attributes", attributes);
        AppleReqBody data = AppleReqBody.init().add("data",body);
        HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<>(data,headers);
        String url = AppleApiEnum.NEW_CERTIFICATES_API.getApiPath();
        ResponseEntity<AppleApiResult<AppleResultDTO>> response = restTemplate.exchange(url,AppleApiEnum.NEW_CERTIFICATES_API.getHttpMethod(),httpEntity,
                new ParameterizedTypeReference<AppleApiResult<AppleResultDTO>>(){});
        return response.getBody().getData();
    }

    public List<AppleResultDTO> selectCertificates(AuthorizeBO authorizeBO){
        HttpHeaders headers = getToken(authorizeBO.getP8(), authorizeBO.getIss(), authorizeBO.getKid());
        AppleReqBody qeryParam = AppleReqBody.init().add("fields[certificates]","IOS_DEVELOPMENT");
        HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<>(qeryParam,headers);
        String url = AppleApiEnum.LIST_CERTIFICATES_API.getApiPath();
        ResponseEntity<AppleApiResult<List<AppleResultDTO>>> response = restTemplate.exchange(url,AppleApiEnum.LIST_CERTIFICATES_API.getHttpMethod(),httpEntity,
                new ParameterizedTypeReference<AppleApiResult<List<AppleResultDTO>>>(){});
        return response.getBody().getData();
    }

    public void removeBundleIds(AuthorizeBO authorizeBO){
        HttpHeaders headers = getToken(authorizeBO.getP8(), authorizeBO.getIss(), authorizeBO.getKid());
        HttpEntity<Map<String,Object>> httpEntity = new HttpEntity<>(headers);
        String url = AppleApiEnum.LIST_BUNDLEID_API.getApiPath();
        ResponseEntity<AppleApiResult<List<AppleResultDTO>>> response = restTemplate.exchange(url,AppleApiEnum.LIST_BUNDLEID_API.getHttpMethod(),httpEntity,
                new ParameterizedTypeReference<AppleApiResult<List<AppleResultDTO>>>(){});

        if(response.getBody().getData()!=null){
            for(AppleResultDTO appleResultDTO:response.getBody().getData()){
                url = AppleApiEnum.DELETE_BUNDLEID_API.getApiPath().replace("{id}",appleResultDTO.getId());
                restTemplate.exchange(url,AppleApiEnum.DELETE_BUNDLEID_API.getHttpMethod(),httpEntity,
                        Void.class);
            }
        }
    }

}
