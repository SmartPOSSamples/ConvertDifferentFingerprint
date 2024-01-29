package com.cloudpos.convertdifffinger.mgr;
import android.content.Context;

import com.upek.android.ptapi.PtException;
import com.upek.android.ptapi.struct.PtInputBir;

public interface IFPMgr {
    void test(Context mContext, int mFingerId);

    PtInputBir enrollFp(Context mContext, int mFingerId);
    boolean verifyAll(Context mContext);

    boolean deleteAll(Context mContext);

    byte[] convertPtInputBirToIso(Context mContext, PtInputBir template);

    PtInputBir convertIsoToPtInputBir(Context mContext, int factorsMask, short formatID, short formatOwner, byte headerVersion, byte purpose, byte quality, byte type,
                                      byte [] mIsoRawTemplate);
    boolean addFinglePrint(Context mContext, PtInputBir template, int mFingerId);

    boolean addFinglePrint(Context mContext, int factorsMask, short formatID, short formatOwner, byte headerVersion, byte purpose, byte quality, byte type,
                           byte [] mIsoRawTemplate, int mFingerId);
    
    boolean verifyEx(Context mContext,PtInputBir[] birs);

	boolean verifyMatch(Context context,PtInputBir bir);
	void open(Context context);
	void close();
	public  byte[] GrabImage(byte byType) throws PtException;
	public int getImagewidth() throws PtException;

}
