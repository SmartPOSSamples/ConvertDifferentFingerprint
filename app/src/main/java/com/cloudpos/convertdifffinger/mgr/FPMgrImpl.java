package com.cloudpos.convertdifffinger.mgr;

import java.io.File;

import android.content.Context;

import com.cloudpos.convertdifffinger.utils.ByteConvert;
import com.cloudpos.convertdifffinger.utils.Logger;
import com.upek.android.ptapi.PtConnectionAdvancedI;
import com.upek.android.ptapi.PtConstants;
import com.upek.android.ptapi.PtException;
import com.upek.android.ptapi.PtGlobal;
import com.upek.android.ptapi.callback.PtGuiStateCallback;
import com.upek.android.ptapi.callback.PtIdleCallback;
import com.upek.android.ptapi.resultarg.ByteArrayArg;
import com.upek.android.ptapi.resultarg.ByteArrayArgI;
import com.upek.android.ptapi.resultarg.IntegerArg;
import com.upek.android.ptapi.resultarg.PtBirArg;
import com.upek.android.ptapi.struct.PtBir;
import com.upek.android.ptapi.struct.PtFingerListItem;
import com.upek.android.ptapi.struct.PtGuiSampleImage;
import com.upek.android.ptapi.struct.PtInfo;
import com.upek.android.ptapi.struct.PtInputBir;
import com.upek.android.ptapi.struct.PtSessionCfgV5;
import com.upek.android.ptapi.usb.PtUsbHost;

public class FPMgrImpl implements IFPMgr, PtGuiStateCallback {

    //This variable configures support for STM32 area reader. This sensor requires additional data storage (temporary file)
    //On emulated environment (emulator + bridge) it must be set to zero, so the default setting will be used
    //On real Android device the default place for storage doesn't work as it must be detected in runtime
    //Set this variable to 1 to enable this behavior
    public static final int miRunningOnRealHardware = 1;
    private PtGlobal mPtGlobal = null;
    private PtConnectionAdvancedI mConn = null;
    private PtInfo mSensorInfo = null;
    private static short SESSION_CFG_VERSION = 5;
    private static FPMgrImpl instance;
    //will contain path for temporary files on real Android device
    private String msNvmPath = null;

    public static FPMgrImpl getInstance() {
        if (instance == null) {
            instance = new FPMgrImpl();
        }
        return instance;
    }

    /**
     * Load PTAPI library and initialize its interface.
     *
     * @return True, if library is ready for use.
     */
    private synchronized boolean initializePtapi(Context aContext) {
        // Load PTAPI library
        mPtGlobal = new PtGlobal(aContext);

        try {
            // Initialize PTAPI interface
            // Note: If PtBridge development technology is in place and a network
            // connection cannot be established, this call hangs forever.
            mPtGlobal.initialize();

            if (miRunningOnRealHardware != 0) {
                //find the directory for temporary files
                File aDir = aContext.getDir("tcstore", Context.MODE_PRIVATE | Context.MODE_PRIVATE);
                if (aDir != null) {
                    msNvmPath = aDir.getAbsolutePath();
                }
            }
            return true;
        } catch (java.lang.UnsatisfiedLinkError ule) {
            // Library wasn't loaded properly during PtGlobal object construction
            Logger.error("libjniPtapi.so not loaded");
            mPtGlobal = null;
            return false;

        } catch (PtException e) {
            Logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Terminate PTAPI library.
     */
    private synchronized void terminatePtapi() {
        try {
            if (mPtGlobal != null) {
                mPtGlobal.terminate();
            }
        } catch (PtException e) {
            //ignore errors
        }
        mPtGlobal = null;
    }


    private synchronized void openPtapiSessionInternal(String dsn, Context aContext) throws PtException {
        // Try to open device with given DSN string
        try {
            PtUsbHost.PtUsbCheckDevice(aContext, 0);
        } catch (PtException e) {
            throw e;
        }
        try {

            mConn = (PtConnectionAdvancedI) mPtGlobal.open(dsn);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Verify that emulated NVM is initialized and accessible
            mSensorInfo = mConn.info();
        } catch (PtException e) {

            if ((e.getCode() == PtException.PT_STATUS_EMULATED_NVM_INVALID_FORMAT) || (e.getCode() == PtException.PT_STATUS_NVM_INVALID_FORMAT) || (e.getCode() == PtException.PT_STATUS_NVM_ERROR)) {
                if (miRunningOnRealHardware != 0) {
                    //try add storage configuration and reopen the device
                    dsn += ",nvmprefix=" + msNvmPath + '/';
                    // Reopen session
                    mConn.close();
                    mConn = null;

                    mConn = (PtConnectionAdvancedI) mPtGlobal.open(dsn);
                    try {
                        // Verify that emulated NVM is initialized and accessible
                        mSensorInfo = mConn.info();
                        configureOpenedDevice();
                        return;
                    } catch (PtException e2) {
                        //ignore errors and continue
                    }
                }


                // We have found the device, but it seems to be either opened for the first time
                // or its emulated NVM was corrupted.
                // Perform the manufacturing procedure.
                //To properly initialize it, we have to:
                // 1. Format its emulated NVM storage
                // 2. Calibrate the sensor

                // Format internal NVM
                mConn.formatInternalNVM(0, null, null);
                // Reopen session
                mConn.close();
                mConn = null;

                mConn = (PtConnectionAdvancedI) mPtGlobal.open(dsn);

                // Verify that emulated NVM is initialized and accessible
                mSensorInfo = mConn.info();
                //check if sensor is calibrated
                if ((mSensorInfo.sensorType & PtConstants.PT_SENSORBIT_CALIBRATED) == 0) {
                    // No, so calibrate it
                    mConn.calibrate(PtConstants.PT_CALIB_TYPE_TURBOMODE_CALIBRATION);
                    // Update mSensorInfo
                    mSensorInfo = mConn.info();
                }

                // Device successfully opened
            } else {
                throw e;
            }
        }

        configureOpenedDevice();
    }

    private synchronized void closeSession() {
        if (mConn != null) {
            try {
                mConn.close();
            } catch (PtException e) {
                // Ignore errors
            }
            mConn = null;
        }
    }

    private void configureOpenedDevice() throws PtException {
        PtSessionCfgV5 sessionCfg = (PtSessionCfgV5) mConn.getSessionCfgEx(PtConstants.PT_CURRENT_SESSION_CFG);
        sessionCfg.sensorSecurityMode = PtConstants.PT_SSM_DISABLED;
        sessionCfg.callbackLevel |= PtConstants.CALLBACKSBIT_NO_REPEATING_MSG;
        mConn.setSessionCfgEx(PtConstants.PT_CURRENT_SESSION_CFG, sessionCfg);
    }


    /**
     * Simple conversion PtBir to PtInputBir
     */
    private PtInputBir makeInputBirFromBir(PtBir aBir) {
        PtInputBir aInputBir = new PtInputBir();
        aInputBir.form = PtConstants.PT_FULLBIR_INPUT;
        aInputBir.bir = aBir;
        return aInputBir;
    }

    @Override
    public boolean verifyMatch(Context context, PtInputBir bir) {
        boolean result = true;//initializePtapi(context);
        if (result) {
            try {
                //           openPtapiSessionInternal("", context);
                if (mConn.verifyMatch(null, null, null, null, bir, null, null, null, null) == true) {
                    return true;
                }

            } catch (PtException e) {
                e.printStackTrace();
            } finally {
//                 closeSession();
//                 terminatePtapi();
            }
        }
        return false;
    }

    @Override
    public boolean verifyAll(Context context) {
        boolean result = initializePtapi(context);
        if (result) {
            try {
                openPtapiSessionInternal("", context);
                // List fingers stored in device
                PtFingerListItem[] fingerList = mConn.listAllFingers();
                if ((fingerList != null) && (fingerList.length > 0)) {
                    // Run verification against all templates stored at device
                    // It is supposed that device contains only templates enrolled by
                    // this sample. In opposite case, verifyEx() with listed templates
                    // would be preferred call.
                    int index = mConn.verifyAll(null, null, null, null, null, null, null, PtConstants.PT_BIO_INFINITE_TIMEOUT, true, null, null, null);
                    if (-1 != index) {
                        // Display finger ID
                        for (int i = 0; i < fingerList.length; i++) {
                            PtFingerListItem item = fingerList[i];
                            int slotId = item.slotNr;

                            if (slotId == index) {
                                byte[] fingerData = item.fingerData;

                                if ((fingerData != null) && (fingerData.length >= 1)) {
                                    int fingerId = item.fingerData[0];
                                    Logger.debug(FingerId.NAMES[fingerId] + " finger matched");
                                } else {
                                    Logger.error("No fingerData set");
                                }
                            }
                        }
                        return true;
                    }
                }

            } catch (PtException e) {
                e.printStackTrace();
            } finally {
                closeSession();
                terminatePtapi();
            }
        }
        return false;
    }

    public boolean verifyEx(Context mContext, PtInputBir[] birs) {
        boolean result = true;//initializePtapi(mContext);
        if (result) {
            try {
                //openPtapiSessionInternal("", mContext);
                short verifyExResult = mConn.verifyEx(null, null, null, birs, null, null, null, null, PtConstants.PT_BIO_INFINITE_TIMEOUT, true, null, null, null);
                Logger.debug("verifyExResult = %s", verifyExResult);
//                Returns:
//                The result: The zero-based index of the best matching template or -1 if no match. (Corresponds with parameter pshResult in original function)
                if (-1 != verifyExResult) {
                    // Display finger ID
                    PtFingerListItem[] fingerList = mConn.listAllFingers();
                    if (fingerList != null) {
                        for (int i = 0; i < fingerList.length; i++) {
                            PtFingerListItem item = fingerList[i];
                            int slotId = item.slotNr;

                            if (slotId == verifyExResult) {
                                byte[] fingerData = item.fingerData;

                                if ((fingerData != null) && (fingerData.length >= 1)) {
                                    int fingerId = item.fingerData[0];
                                    Logger.debug(FingerId.NAMES[fingerId] + " finger matched");
                                } else {
                                    Logger.error("No fingerData set");
                                }
                            }
                        }
                    }
                    return true;
                }

            } catch (PtException e) {
                e.printStackTrace();
            } finally {
                //  closeSession();
                //   terminatePtapi();
            }
        }
        return false;
    }

    public byte[] GrabImage(byte byType) throws PtException {
        // Register notification callback of operation state
        // Valid for entire PTAPI session lifetime
        //-------------------------------------------------------------------------------------
        //mConn.setGUICallbacks(null, this);

        //return mConn.grab(byType,PtConstants.PT_BIO_INFINITE_TIMEOUT,true,null,null);

        // for sleepThenGrab
        IntegerArg wakeUpCause = new IntegerArg();
        wakeUpCause.setValue(PtConstants.PT_WAKEUP_CAUSE_FINGER);//PtConstants.PT_WAKEUP_CAUSE_FINGER=1;PT_WAKEUP_CAUSE_HOST
        IntegerArg grabGuiMessage = new IntegerArg();
        ByteArrayArgI grabData = new ByteArrayArg();
        try {
            PtIdleCallback idleCallback = new PtIdleCallback() {

                @Override
                public byte idleCallbackInvoke() throws PtException {
                    // TODO Auto-generated method stub
                    return PtConstants.PT_CONTINUE;
                }

            };
            mConn.sleepThenGrab(idleCallback, byType, -1, true, wakeUpCause, grabGuiMessage, grabData, null, null);
//             onDisplayMessage("wakeupCause "+wakeUpCause.getValue()+" gui "+grabGuiMessage.getValue());

            if ((wakeUpCause.getValue() == PtConstants.PT_WAKEUP_CAUSE_FINGER) && (grabGuiMessage.getValue() == PtConstants.PT_GUIMSG_GOOD_IMAGE))
                return grabData.getValue();
            else
                return null;
        } catch (PtException e) {
//                onDisplayMessage("Grab failed - " + e.getMessage());
            throw e;
        }
    }

    public int getImagewidth() throws PtException {
        return (mConn.info().imageWidth);
    }

    @Override
    public boolean deleteAll(Context mContext) {
        boolean result = true;//initializePtapi(mContext);
        if (result) {
            try {
                // openPtapiSessionInternal("", mContext);
                mConn.deleteAllFingers();
            } catch (PtException e) {
                e.printStackTrace();
            } finally {
                //closeSession();
                //terminatePtapi();
            }
        }
        return false;
    }


    public boolean addFinglePrint(Context mContext, PtInputBir template, int mFingerId) {
        boolean result = true;// initializePtapi(mContext);
        if (result) {
            try {
                //openPtapiSessionInternal("", mContext);
                if (checkFp(mFingerId)) {
                    // Store enrolled template and finger ID to device
                    addFinger(template, mFingerId);
                    return true;
                } else {
                    Logger.error("enrollFp failed! reason: check fingerprint failed!");
                }
                // Un-cancel session
                mConn.cancel(1);
            } catch (PtException e) {
                e.printStackTrace();
            } finally {
                //    closeSession();
                //    terminatePtapi();
            }
        }
        return false;
    }

    @Override
    public boolean addFinglePrint(Context mContext, int factorsMask, short formatID, short formatOwner, byte headerVersion, byte purpose, byte quality, byte type, byte[] mIsoRawTemplate, int mFingerId) {
        PtInputBir templateByIso = convertIsoToPtInputBir(mContext, factorsMask, formatID, formatOwner, headerVersion, purpose, quality, type, mIsoRawTemplate);
        return addFinglePrint(mContext, templateByIso, mFingerId);
    }

    @Override
    public byte[] convertPtInputBirToIso(Context mContext, PtInputBir template) {
        //Convert just enrolled template to ISO template
        boolean result = true;//initializePtapi(mContext);
        if (result) {
            try {
                //openPtapiSessionInternal("", mContext);
                byte[] mIsoRawTemplate = mConn.convertTemplateEx(PtConstants.PT_TEMPLATE_TYPE_AUTO, PtConstants.PT_TEMPLATE_ENVELOPE_NONE, template.bir.data, PtConstants.PT_TEMPLATE_TYPE_ISO_FMR, PtConstants.PT_TEMPLATE_ENVELOPE_NONE, null, 0);
                return mIsoRawTemplate;
            } catch (PtException e) {
                e.printStackTrace();
            } finally {
                //closeSession();
                //terminatePtapi();
            }
        }
        return null;
    }

    @Override
    public void test(Context mContext, int mFingerId) {
        boolean result = initializePtapi(mContext);
        if (result) {
            try {
                openPtapiSessionInternal("", mContext);

                mConn.deleteAllFingers();

                // Optional: Set session configuration to enroll 3-5 swipes instead of 5-10
                modifyEnrollmentType(3, 5);
                // Obtain finger template
                PtInputBir template = enroll();
                if (checkFp(mFingerId)) {
                    // Store enrolled template and finger ID to device
                    addFinger(template, mFingerId);

                    //Convert just enrolled template to ISO template
                    byte[] mIsoRawTemplate = mConn.convertTemplateEx(PtConstants.PT_TEMPLATE_TYPE_AUTO, PtConstants.PT_TEMPLATE_ENVELOPE_NONE, template.bir.data, PtConstants.PT_TEMPLATE_TYPE_ISO_FMR, PtConstants.PT_TEMPLATE_ENVELOPE_NONE, null, 0);

                    Logger.error("test : " + ByteConvert.getBestString(mIsoRawTemplate));

                    PtFingerListItem[] fingerList = mConn.listAllFingers();

                    if (fingerList != null) {
                        for (int i = 0; i < fingerList.length; i++) {
                            PtFingerListItem item = fingerList[i];
                            byte[] bytes = mConn.getFingerData(item.slotNr);
                            Logger.error("fingerList : " + ByteConvert.getBestString(bytes));
                        }
                    }
//                    PtBir mConn.convertTemplate()

                    PtInputBir templateBack = convertIsoToPtInputBir(template.bir.factorsMask, template.bir.formatID, template.bir.formatOwner, template.bir.headerVersion, template.bir.purpose, template.bir.quality, template.bir.type, mIsoRawTemplate);

                    checkFp(mFingerId);// delete fingleprint and add
                    // Store enrolled template and finger ID to device
                    addFinger(templateBack, mFingerId);

                    fingerList = mConn.listAllFingers();

                    if (fingerList != null) {
                        for (int i = 0; i < fingerList.length; i++) {
                            PtFingerListItem item = fingerList[i];
                            byte[] bytes = mConn.getFingerData(item.slotNr);
                            Logger.error("fingerList : " + ByteConvert.getBestString(bytes));
                        }
                    }

                } else {
                    Logger.error("enrollFp failed! reason: check fingerprint failed!");
                }
                // Un-cancel session
                mConn.cancel(1);
            } catch (PtException e) {
                e.printStackTrace();
            } finally {
                closeSession();
                terminatePtapi();
            }
        }

    }

    @Override
    public PtInputBir convertIsoToPtInputBir(Context mContext, int factorsMask, short formatID, short formatOwner, byte headerVersion, byte purpose, byte quality, byte type, byte[] mIsoRawTemplate) {
        boolean result = true;//initializePtapi(mContext);
        if (result) {
            try {
                //  openPtapiSessionInternal("", mContext);
                PtInputBir templateByIso = convertIsoToPtInputBir(factorsMask, formatID, formatOwner, headerVersion, purpose, quality, type, mIsoRawTemplate);
                return templateByIso;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // closeSession();
                // terminatePtapi();
            }
        }

        return null;
    }

    private PtInputBir convertIsoToPtInputBir(int factorsMask, short formatID, short formatOwner, byte headerVersion, byte purpose, byte quality, byte type, byte[] mIsoRawTemplate) {
        Logger.error("factorsMask=%s, formatID=%s, formatOwner=%s, headerVersion=%s, purpose=%s, quality=%s, type=%s", factorsMask, formatID, formatOwner, headerVersion, purpose, quality, type);
        //Convert ISO template back to alpha template, get raw template (without header)
        byte[] aAlphaRawTemplate = new byte[0];
        try {
            aAlphaRawTemplate = mConn.convertTemplateEx(PtConstants.PT_TEMPLATE_TYPE_ISO_FMR, PtConstants.PT_TEMPLATE_ENVELOPE_NONE, mIsoRawTemplate, PtConstants.PT_TEMPLATE_TYPE_ALPHA, PtConstants.PT_TEMPLATE_ENVELOPE_NONE, null, 0);
            //        factorsMask=8, formatID=0, formatOwner=18, headerVersion=1, purpose=3, quality=-2, type=4
//        factorsMask=8, formatID=0, formatOwner=18, headerVersion=1, purpose=3, quality=-2, type=4
//        factorsMask=8, formatID=0, formatOwner=18, headerVersion=1, purpose=3, quality=-2, type=4
//        factorsMask=8, formatID=0, formatOwner=18, headerVersion=1, purpose=3, quality=-2, type=4
            //Create template with header
            PtBir aAlphaBir = new PtBir();
            aAlphaBir.factorsMask = factorsMask;
            aAlphaBir.formatID = formatID;
            aAlphaBir.formatOwner = formatOwner;
            aAlphaBir.headerVersion = headerVersion;
            aAlphaBir.purpose = headerVersion;
            aAlphaBir.quality = headerVersion;
            aAlphaBir.type = type;

            //add raw alpha template data to this header, round up size to 4 and add empty payload (another 4 zero bytes)
            aAlphaBir.data = new byte[4 + ((aAlphaRawTemplate.length + 3) & ~3)];
            //copy raw template
            for (int i = 0; i < aAlphaRawTemplate.length; i++) {
                aAlphaBir.data[i] = aAlphaRawTemplate[i];
            }
            //fill additional zero bytes
            for (int i = aAlphaRawTemplate.length; i < aAlphaBir.data.length; i++) {
                aAlphaBir.data[i] = 0;
            }

            //Convert PtBir to PtInputBir
            PtInputBir aAlphaInputBir = makeInputBirFromBir(aAlphaBir);
            return aAlphaInputBir;
        } catch (PtException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public PtInputBir enrollFp(Context mContext, int mFingerId) {
        boolean result = true;//initializePtapi(mContext);
        if (result) {
            try {
                //   openPtapiSessionInternal("", mContext);
                // Optional: Set session configuration to enroll 3-5 swipes instead of 5-10
                modifyEnrollmentType(3, 5);
                // Obtain finger template
                PtInputBir template = enroll();
                if (checkFp(mFingerId)) {
                    // Store enrolled template and finger ID to device
                    addFinger(template, mFingerId);
                    return template;
                } else {
                    Logger.error("enrollFp failed! reason: check fingerprint failed!");
                }
                // Un-cancel session
                mConn.cancel(1);
            } catch (PtException e) {
                e.printStackTrace();
            } finally {
                //          closeSession();
                //          terminatePtapi();
            }
        }

        return null;
    }

    /**
     * Modify enrollment to 3-5 swipes.
     */
    private void modifyEnrollmentType(int min, int max) throws PtException {
        try {
            PtSessionCfgV5 sessionCfg = (PtSessionCfgV5) mConn.getSessionCfgEx(SESSION_CFG_VERSION);
            sessionCfg.enrollMinTemplates = (byte) min;
            sessionCfg.enrollMaxTemplates = (byte) max;
            mConn.setSessionCfgEx(SESSION_CFG_VERSION, sessionCfg);
        } catch (PtException e) {
            Logger.error("Unable to set session cfg - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Store enrolled template and finger ID to device
     *
     * @param template  InputBir.
     * @param mFingerId
     */
    private void addFinger(PtInputBir template, int mFingerId) {
        try {
            //store template
            int slot = mConn.storeFinger(template);

            //store fingerId
            byte[] fingerData = new byte[1];
            fingerData[0] = (byte) mFingerId;
            mConn.setFingerData(slot, fingerData);
        } catch (PtException e) {
            e.printStackTrace();
//            onDisplayMessage("addFinger failed - " + e.getMessage());
        }
    }

    private boolean checkFp(int mFingerId) {
        // List fingers stored in device
        PtFingerListItem[] fingerList = null;
        try {
            fingerList = mConn.listAllFingers();
            if (fingerList != null) {
                for (int i = 0; i < fingerList.length; i++) {
                    PtFingerListItem item = fingerList[i];
                    byte[] fingerData = item.fingerData;

                    if ((fingerData != null) && (fingerData.length >= 1)) {
                        int fingerId = item.fingerData[0];
                        if (fingerId == mFingerId) {
                            // Delete finger from device
                            mConn.deleteFinger(item.slotNr);
                        } else {
                            PtInputBir comparedBir = new PtInputBir();
                            comparedBir.form = PtConstants.PT_SLOT_INPUT;
                            comparedBir.slotNr = item.slotNr;

                            // Verify, if template doesn't match the enrolled one (last good template)
                            if (mConn.verifyMatch(null, null, null, null, comparedBir, null, null, null, null) == true) {
                                Logger.error("Finger already enrolled as " + FingerId.NAMES[fingerId]);
                                return false;
                            }
                        }
                    }
                }
            }
        } catch (PtException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Obtain finger template.
     */
    private PtInputBir enroll() throws PtException {
        PtBirArg newTemplate = new PtBirArg();

        try {
            // Register notification callback of operation state
            // Valid for entire PTAPI session lifetime
            mConn.setGUICallbacks(null, this);

            // Enroll finger, don't store template directly to device but return it to host
            // to allow verification, if finger isn't already enrolled
            mConn.capture(PtConstants.PT_PURPOSE_VERIFY, newTemplate, PtConstants.PT_BIO_INFINITE_TIMEOUT, null, null, null);
//            mConn.enroll(PtConstants.PT_PURPOSE_ENROLL, null, newTemplate,
//                    null, null, PtConstants.PT_BIO_INFINITE_TIMEOUT,
//                    null, null, null);
//            onDisplayMessage("=========enroll completed===========");
        } catch (PtException e) {
//            onDisplayMessage("Enrollment failed - " + e.getMessage());
            throw e;
        }

        // Convert obtained BIR to INPUT BIR class
        return makeInputBirFromBir(newTemplate.value);
    }

    @Override
    public byte guiStateCallbackInvoke(int guiState, int message, byte progress, PtGuiSampleImage sampleBuffer, byte[] data) {
        String s = PtHelper.GetGuiStateCallbackMessage(guiState, message, progress);

        if (s != null) {
//            onDisplayMessage(s);
            Logger.debug(s);
        }

        // With sensor only solution isn't necessary because of PtCancel() presence
//        return isInterrupted() ? PtConstants.PT_CANCEL : PtConstants.PT_CONTINUE;
        return PtConstants.PT_CONTINUE;
    }

    public void open(Context aContext) {
        try {
            boolean result = initializePtapi(aContext);
            if (result) {
                openPtapiSessionInternal("", aContext);
            }
        } catch (PtException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        closeSession();
        terminatePtapi();
    }
}
