package com.weihuagu.receiptnotice.utils;
import com.weihuagu.receiptnotice.core.IDataTrans;

import java.util.Map;
public abstract class Encrypter implements IDataTrans {
protected String key;
public Encrypter(String key){
this.key=key;
}
public abstract Map<String,String> transferMapValue(Map<String, String> params);




}
