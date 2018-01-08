package com.fiberg.wsio.handler.time;

import java.util.List;

public interface WsIOTime {

	List<WsIOInstant> getTimes();

	void setTimes(List<WsIOInstant> times);

}
