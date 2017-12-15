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

	List<WsIOElement> getSuccessfuls();

	void setSuccessfuls(List<WsIOElement> successfuls);

	List<WsIOElement> getFailures();

	void setFailures(List<WsIOElement> failures);

	List<WsIOElement> getWarnings();

	void setWarnings(List<WsIOElement> warnings);

	Boolean getShowSuccessfuls();

	void setShowSuccessfuls(Boolean ShowSuccessfuls);

	Boolean getShowFailures();

	void setShowFailures(Boolean ShowFailures);

	Boolean getShowWarnings();

	void setShowWarnings(Boolean ShowWarnings);

}
