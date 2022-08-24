package com.example.instalive.app.web.bridge;

public interface BridgeHandler {

	int STATUS_SUCCESS = 0;

	int STATUS_FAILURE = 1;

	void handler(String data, CallBackFunction function);

}
