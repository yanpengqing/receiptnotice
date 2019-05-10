package com.weihuagu.receiptnotice.core;

import java.util.List;

public interface AsyncResponse {
	public void onDataReceivedSuccess(String[] returnstr);
    public  void onDataReceivedFailed(String[] returnstr);
}
