package com.company.ecommerce.constants;

/**
 * 合同模块调用接口
 *
 * @author hzq
 * @date 2019/12/5
 **/
public interface ContractEndpoint {


    /**
     * 创建用户
     */
    String CREATE_USER = "/user/create";

    /**
     * 修改用户
     */
    String UPDATE_USER = "/user/update";

    /**
     * 用户实名
     */
    String REAL_NAME = "/user/realName";


    /**
     * 创建企业
     */
    String CREATE_ENTERPRISE = "/enterprise/create";

    /**
     * 创建企业
     */
    String UPDATE_ENTERPRISE = "/enterprise/update";


    /**
     * 创建印章
     */
    String CREATE_SEAL = "/enterprise/createSeal";
    /**
     * 企业实名认证
     */
    String ENTERPRISE_REALNAME = "/enterprise/realName";
    /**
     * 银行打款验证
     */
    String ENTERPRISE_VERIFYPAYBANK = "/enterprise/verifyPayBank";
    /**
     * 添加内部企业
     */
    String ADD_SUB_COMPANY = "/enterprise/addSubCompany";
    /**
     * 分页查询企业信息
     */
    String PAGE_QUERY_COMPANY = "/enterprise/pageQuery";
    /**
     * 分页查询用户信息
     */
    String PAGE_QUERY_USER = "/user/pageQuery";
    /**
     * 加入企业
     */
    String JOINENTERPRISE = "/user/joinEnterprise";
    /**
     * 创建签名
     */
    String CREATE_AUTO = "/user/createAutograph";
    /**
     * 印章授权用户
     */
    String AUTH_SEAL = "/user/authSeal";
    /**
     * 印章授权用户
     */
    String APPLY_CERT = "/user/applyCert";
    /**
     * 设置签署密码
     */
    String USER_SIGN_PASSWORD = "/user/resetPassword";

    /**
     * 上传文件创建合同
     */
    String CREATE_CONTRACT_BYFILE = "/contract/createByFile";

    /**
     * 获取odf印章hash
     */
    String GET_OFD_SEAL_HASH = "/ofdUkey/getOfdSealHash";
    /**
     * 合成ofd印章
     */
    String BUILD_OFD_SEAL = "/ofdUkey/buildOfdSeal";
    /**
     * 获取odf合同hash
     */
    String GET_OFD_CONTRACT_HASH = "/ofdUkey/getOfdContractHash";
    /**
     * 合成ofd合同
     */
    String BUILD_OFD_CONTRACT = "/ofdUkey/buildOfdContract";
    /**
     * 下载合同
     */

    String DOWNLOAD_CONTRACT = "/contract/downloadContract";

    /**
     * 结束签署
     */
    String FINISHED_CONTRACT = "/contract/finished";

    /**
     * 合同详情查询
     */
    String SEARCH_CONTRACT = "/contract/search";
//    String SEARCH_CONTRACT = "contract/getAddSealWebUrl";


    /**
     * 上传合同文件
     */
    String CONTRACT_UPLOAD_FILE = "/contract/uploadFile";
    /**
     * 发起上传文件合同
     */
    String CONTRACT_SEND = "/contract/send";
    /**
     * 通过模板创建合同
     */
    String CONTRACT_CREATE_BY_TEMPLATE = "/contract/createByTemplate";
    /**
     * 添加上传合同签署人
     */
    String CONTRACT_ADD_SIGNER_BY_FILE = "/contract/addSignerByFile";

    /**
     * 上传文件合同后台签署
     */
    String CONTRACT_SIGN_BY_FILE = "/contract/signByFile";

    /**
     * 模板创建合同后台签署
     */
    String USER_SEND_SMSCODE = "/user/sendSmsCode";

    /**
     * Ukey签获取合同hash接口
     */
    String GET_SIGN_DATA = "/contract/getSignData";
    /**
     * Ukey签获取合同hash接口(新)
     */
    String NEW_GET_SIGN_DATA = "/ukeyCert/getSignData";
    /**
     * 合同UKey签署
     */
    String SIGN_BY_UKEY = "/contract/signByUkey";
    /**
     * 合同UKey签署(新)
     */
    String NEW_SIGN_BY_UKEY = "/ukeyCert/signByUkey";

    /**
     * 添加模板合同签署人
     */
    String CONTRACT_ADD_SIGNER_BY_TEMPLATE = "/contract/addSignerByTemplate";

    /**
     * 模板创建合同后台签署
     */
    String CONTRACT_SIGN_BY_TEMPLATE = "/contract/signByTemplate";

    /**
     * 添加合同抄送人
     */
    String CONTRACT_ADD_CC = "/contract/addContractCc";

    /**
     * 合同撤回
     */
    String CONTRACT_URGE_SIGN = "/contract/urgeSign";

    /**
     * 合同撤回
     */
    String DELETE_CONTRACT = "/contract/deleteContract";

    /**
     * 合同撤回
     */
    String CONTRACT_REVOKE = "/contract/revoke";

    /**
     * 获取关键字坐标
     */
    String GET_KEYWORD_COORDINATES = "/contract/getKeywordCoordinates";

    /**
     * 获取合同列表
     */
    String CONTRACT_LIST = "/contract/contractList";
    /**
     * 模板列表
     */
    String TEMPLATE_PAGE_LIST = "/contractTemplate/templatePageList";
    /**
     * 模板详情
     */
    String TEMPLATE_DETAILS = "/contractTemplate/templateDetails";
    /**
     * 模板删除
     */
    String TEMPLATE_DELETE = "/contractTemplate/delete";

    /**
     * 创建模板分类
     */
    String TEMPLATE_CREATE_TYPE_NODE = "/contractTemplate/createTypeNode";

    /**
     * 获取模板分类
     */
    String TEMPLATE_GET_TYPE_NODE = "/contractTemplate/getTypeNode";

    /**
     * 创建模板
     */
    String CREATE_TEMPLATE = "/contractTemplate/createTemplate";

    /**
     * 上传模板合同文件
     */
    String UPLOAD_TEMPLATE_FILE = "/contractTemplate/uploadFile";

    /**
     * 模板设置签署人签署控件
     */
    String TEMPLATE_SET_SIGN_CONTROLS = "/contractTemplate/setSignControls";

    /**
     * 编辑模板
     */
    String UPDATE_TEMPLATE = "/contractTemplate/updateTemplate";

    /**
     * 启用、禁用模板
     */
    String ON_OR_OFF_TEMPLATE = "/contractTemplate/onOrOffTemplate";
    /**
     * 快捷签署
     */
    String FAST_EASY_SIGN = "/easySign/easySignContract";
    /**
     * 上传文件合同-发起页面签接口
     */
    String START_SIGN_BY_FILE = "/pageSign/startSignByFile";
    /**
     * 批量签署
     */
    String BATCH_SIGN = "/contract/batchSign";
    /**
     * 合同验签
     */
    String VERIFY_CONTRACT = "/contract/verifyContract";

    /**
     * 合同快捷签（同步）fastSign
     */
    String FAST_SIGN = "/contract/fastSign";

    /**
     * 获取签名列表
     */
    String STAMP_LIST = "/stamp/stampList";

    /**
     * 获取印章列表
     */
    String SEAL_LIST = "/seal/sealList";

    /**
     * 印章状态管理
     */
    String SEAL_STATUS_MANAGER = "/seal/sealStatusManager";

    /**
     * 印章删除
     */
    String DELETE_SEAL = "/seal/deleteSeal";


    /**
     * 获取授权
     */
    String SAVE_AUTHORIZATION_RECORD = "/authorizationRecord/saveAuthorizationRecord";

    /**
     * 开启api页面
     */
    String START_AUTHORIZATION_PAGE = "/authorizationRecord/startAuthorizationPage";


    String ADD_OR_MODIFY_ADMIN = "/enterprise/addOrModifyAdmin";


    String INTEND_TO_CERTIFICATION = "/willingness/intendToCertification";

    String REMOVE_USER_FROM_ENTERPRISE = "/enterprise/removeOrgUser";

    String  INSERT_CERTCONFIG_TRIPARTITE = "/sysServiceConfig/insertCertConfigTripartite";

    /**
     * 合同解约接口
     */
    String CONTRACT_NULLIFY ="/contract/contractNullify";


    /**
     * 合同解约接口
     */
    String getPdfText ="/hide/getPdfText";


//    String getPdfText ="/hide/getPdfText";


    /**
     * 模板创建合同-发起页面签
     */
    String START_SIGN_BY_FILE_TEMPLATE = "/pageSign/startSignByTemplateFile";

}
