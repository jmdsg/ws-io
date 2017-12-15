package com.fiberg.wsio.handler.time;

import java.util.List;

public interface WsIOTime {

	List<WsIOInstant> getDateTimes();

	void setDateTimes(List<WsIOInstant> dateTimes);

}
