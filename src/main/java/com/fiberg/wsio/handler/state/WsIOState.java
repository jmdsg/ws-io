package com.fiberg.wsio.handler.state;

import java.util.List;

public interface WsIOState {

	String getIdentifier();

	void setIdentifier(String identifier);

	WsIOText getMessage();

	void setMessage(WsIOText message);

	WsIOText getDescription();

	void setDescription(WsIOText description);

	String getType();

	void setType(String type);

	WsIOStatus getStatus();

	void setStatus(WsIOStatus status);

	WsIODetail getDetail();

	void setDetail(WsIODetail detail);

	List<WsIOItem> getSuccessfulItems();

	void setSuccessfulItems(List<WsIOItem> successfulItems);

	List<WsIOItem> getFailureItems();

	void setFailureItems(List<WsIOItem> failureItems);

	List<WsIOItem> getWarningItems();

	void setWarningItems(List<WsIOItem> warningItems);

	Boolean getShowSuccessfulItems();

	void setShowSuccessfulItems(Boolean ShowSuccessfulItems);

	Boolean getShowFailureItems();

	void setShowFailureItems(Boolean ShowFailureItems);

	Boolean getShowWarningItems();

	void setShowWarningItems(Boolean ShowWarningItems);

}
