package com.naoh.iossupersign.service.Ipapackage.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naoh.iossupersign.model.po.IpaPackagePO;
import com.naoh.iossupersign.service.Ipapackage.IpaPackageService;
import com.naoh.iossupersign.storage.mysql.mapper.IpaPackageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Peter.Hong
 * @date 2019/12/11
 */
@Service
public class IpaPackageServiceImpl implements IpaPackageService {

    @Autowired
    private IpaPackageMapper ipaPackageMapper;

    @Override
    public void insertIpaPackage(IpaPackagePO ipaPackagePO) {
        ipaPackageMapper.insert(ipaPackagePO);
    }

    @Override
    public void updateIpaPackage(IpaPackagePO ipaPackagePO) {
        ipaPackageMapper.updateById(ipaPackagePO);
    }

    @Override
    public Page<IpaPackagePO> getIpaPackagePage(Page<IpaPackagePO> page) {
        return ipaPackageMapper.selectPage(page);
    }
}