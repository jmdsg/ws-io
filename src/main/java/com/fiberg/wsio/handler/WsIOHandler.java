package com.fiberg.wsio.handler;

import com.fiberg.wsio.handler.state.*;
import com.fiberg.wsio.handler.time.WsIOInstant;
import com.fiberg.wsio.handler.time.WsIOTime;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class WsIOHandler {

	public static final class Time {

		private Time() {  }

		private static final ThreadLocal<Boolean> enabled = new ThreadLocal<>();

		private static final ThreadLocal<List<WsIOInstant>> times = new ThreadLocal<>();

		public static void transfer(WsIOTime timeWrapper) {
			if (timeWrapper != null) {
				if (times.get() != null) {
					timeWrapper.setTimes(times.get());
				}
			}
		}

		public static void clear() {
			clearTimes();
		}

		public static void reset() {
			resetTimes();
		}

		public static boolean isEnabled() {
			return Boolean.TRUE.equals(getEnabled());
		}

		public static Boolean getEnabled() {
			return enabled.get();
		}

		public static void setEnabled(Boolean enabled) {
			State.enabled.set(enabled);
		}

		public static List<WsIOInstant> getTimes() {
			return Time.times.get();
		}

		public static void setTimes(List<WsIOInstant> times) {
			Time.times.set(times);
		}

		public static void clearTimes() {
			if (Time.times.get() != null) {
				Time.times.get().clear();
			}
		}

		public static void resetTimes() {
			Time.times.set(null);
		}

		public static boolean addDateTime(WsIOInstant dateTime) {
			if (Time.times.get() == null) {
				Time.times.set(new ArrayList<>());
			}
			return Time.times.get().add(dateTime);
		}

		public static boolean addDateTimeOf(String id, ZonedDateTime time) {
			if (Time.times.get() == null) {
				Time.times.set(new ArrayList<>());
			}
			return Time.times.get().add(WsIOInstant.of(id, time));
		}

		public static boolean removeDateTime(WsIOInstant dateTime) {
			if (Time.times.get() != null) {
				return Time.times.get().remove(dateTime);
			}
			return false;
		}

		public static boolean removeDateTimeOf(String id, ZonedDateTime time) {
			if (Time.times.get() != null) {
				return Time.times.get().remove(WsIOInstant.of(id, time));
			}
			return false;
		}

		public static boolean addDateTimeUnnamed(ZonedDateTime time) {
			if (Time.times.get() == null) {
				Time.times.set(new ArrayList<>());
			}
			return Time.times.get().add(WsIOInstant.unnamed(time));
		}

		public static boolean removeDateTimeUnnamed(ZonedDateTime time) {
			if (Time.times.get() == null) {
				Time.times.set(new ArrayList<>());
			}
			return Time.times.get().remove(WsIOInstant.unnamed(time));
		}

		public static boolean addDateTimeNow(String id) {
			if (Time.times.get() == null) {
				Time.times.set(new ArrayList<>());
			}
			return Time.times.get().add(WsIOInstant.now(id));
		}

		public static boolean addDateTimeNow() {
			if (Time.times.get() == null) {
				Time.times.set(new ArrayList<>());
			}
			return Time.times.get().add(WsIOInstant.now());
		}

	}

	public static final class State {

		private State() {}

		private static final ThreadLocal<Boolean> enabled = new ThreadLocal<>();

		private static final ThreadLocal<String> code = new ThreadLocal<>();

		private static final ThreadLocal<String> identifier = new ThreadLocal<>();

		private static final ThreadLocal<WsIOText> message = new ThreadLocal<>();

		private static final ThreadLocal<WsIOText> description = new ThreadLocal<>();

		private static final ThreadLocal<String> type = new ThreadLocal<>();

		private static final ThreadLocal<WsIOStatus> status = new ThreadLocal<>();

		private static final ThreadLocal<WsIODetail> detail = new ThreadLocal<>();

		private static final ThreadLocal<List<WsIOItem>> successfulItems = new ThreadLocal<>();

		private static final ThreadLocal<List<WsIOItem>> failureItems = new ThreadLocal<>();

		private static final ThreadLocal<List<WsIOItem>> warningItems = new ThreadLocal<>();

		private static final ThreadLocal<Boolean> showSuccessfulItems = new ThreadLocal<>();

		private static final ThreadLocal<Boolean> showFailureItems = new ThreadLocal<>();

		private static final ThreadLocal<Boolean> showWarningItems = new ThreadLocal<>();

		private static final ThreadLocal<WsIOLanguage> defaultLanguage = new ThreadLocal<>();

		public static void transfer(WsIOState stateWrapper) {
			if (stateWrapper != null) {
				if (identifier.get() != null) {
					stateWrapper.setIdentifier(identifier.get());
				}
				if (message.get() != null) {
					stateWrapper.setMessage(message.get());
				}
				if (description.get() != null) {
					stateWrapper.setDescription(description.get());
				}
				if (type.get() != null) {
					stateWrapper.setType(type.get());
				}
				if (status.get() != null) {
					stateWrapper.setStatus(status.get());
				}
				if (detail.get() != null) {
					stateWrapper.setDetail(detail.get());
				}
				if (successfulItems.get() != null) {
					stateWrapper.setSuccessfulItems(successfulItems.get());
				}
				if (failureItems.get() != null) {
					stateWrapper.setFailureItems(failureItems.get());
				}
				if (warningItems.get() != null) {
					stateWrapper.setWarningItems(warningItems.get());
				}
				if (showSuccessfulItems.get() != null) {
					stateWrapper.setShowSuccessfulItems(showSuccessfulItems.get());
				}
				if (showFailureItems.get() != null) {
					stateWrapper.setShowFailureItems(showFailureItems.get());
				}
				if (showWarningItems.get() != null) {
					stateWrapper.setShowWarningItems(showWarningItems.get());
				}
			}
		}

		public static void clear() {
			clearSuccessfulItems();
			clearFailureItems();
			clearWarningItems();
		}

		public static void reset() {
			resetEnabled();
			resetCode();
			resetIdentifier();
			resetMessage();
			resetDescription();
			resetType();
			resetStatus();
			resetDetail();
			resetSuccessfulItems();
			resetFailureItems();
			resetWarningItems();
			resetShowSuccessfulItems();
			resetShowFailureItems();
			resetShowWarningItems();
			resetDefaultLanguage();
		}

		public static boolean isEnabled() {
			return Boolean.TRUE.equals(getEnabled());
		}

		public static Boolean getEnabled() {
			return enabled.get();
		}

		public static void setEnabled(Boolean enabled) {
			State.enabled.set(enabled);
		}

		public static String getCode() {
			return code.get();
		}

		public static void setCode(String code) {
			State.code.set(code);
		}

		public static String getIdentifier() {
			return identifier.get();
		}

		public static void setIdentifier(String identifier) {
			State.identifier.set(identifier);
		}

		public static WsIOText getMessage() {
			return message.get();
		}

		public static void setMessage(WsIOText message) {
			State.message.set(message);
		}

		public static WsIOText getDescription() {
			return description.get();
		}

		public static void setDescription(WsIOText description) {
			State.description.set(description);
		}

		public static String getType() {
			return type.get();
		}

		public static void setType(String type) {
			State.type.set(type);
		}

		public static WsIOStatus getStatus() {
			return status.get();
		}

		public static void setStatus(WsIOStatus status) {
			State.status.set(status);
		}

		public static WsIODetail getDetail() {
			return detail.get();
		}

		public static void setDetail(WsIODetail detail) {
			State.detail.set(detail);
		}

		public static List<WsIOItem> getSuccessfulItems() {
			return successfulItems.get();
		}

		public static void setSuccessfulItems(List<WsIOItem> successfulItems) {
			State.successfulItems.set(successfulItems);
		}

		public static List<WsIOItem> getFailureItems() {
			return failureItems.get();
		}

		public static void setFailureItems(List<WsIOItem> failureItems) {
			State.failureItems.set(failureItems);
		}

		public static List<WsIOItem> getWarningItems() {
			return warningItems.get();
		}

		public static void setWarningItems(List<WsIOItem> warningItems) {
			State.warningItems.set(warningItems);
		}

		public static Boolean getShowSuccessfulItems() {
			return showSuccessfulItems.get();
		}

		public static void setShowSuccessfulItems(Boolean showSuccessfulItems) {
			State.showSuccessfulItems.set(showSuccessfulItems);
		}

		public static Boolean getShowFailureItems() {
			return showFailureItems.get();
		}

		public static void setShowFailureItems(Boolean showFailureItems) {
			State.showFailureItems.set(showFailureItems);
		}

		public static Boolean getShowWarningItems() {
			return showWarningItems.get();
		}

		public static void setShowWarningItems(Boolean showWarningItems) {
			State.showWarningItems.set(showWarningItems);
		}

		public static WsIOLanguage getDefaultLanguage() {
			return defaultLanguage.get();
		}

		public static void setDefaultLanguage(WsIOLanguage defaultLanguage) {
			State.defaultLanguage.set(defaultLanguage);
		}

		public static void resetEnabled() {
			State.enabled.set(null);
		}

		public static void resetCode() {
			State.code.set(null);
		}

		public static void resetIdentifier() {
			State.identifier.set(null);
		}

		public static void resetMessage() {
			State.message.set(null);
		}

		public static void resetDescription() {
			State.description.set(null);
		}

		public static void resetType() {
			State.type.set(null);
		}

		public static void resetStatus() {
			State.status.set(null);
		}

		public static void resetDetail() {
			State.detail.set(null);
		}

		public static void resetSuccessfulItems() {
			State.successfulItems.set(null);
		}

		public static void resetFailureItems() {
			State.failureItems.set(null);
		}

		public static void resetWarningItems() {
			State.warningItems.set(null);
		}

		public static void resetShowSuccessfulItems() {
			State.showSuccessfulItems.set(null);
		}

		public static void resetShowFailureItems() {
			State.showFailureItems.set(null);
		}

		public static void resetShowWarningItems() {
			State.showWarningItems.set(null);
		}

		public static void resetDefaultLanguage() {
			State.defaultLanguage.set(null);
		}

		public static void clearSuccessfulItems() {
			if (State.successfulItems.get() != null) {
				State.successfulItems.get().clear();
			}
		}

		public static void clearFailureItems() {
			if (State.failureItems.get() != null) {
				State.failureItems.get().clear();
			}
		}

		public static void clearWarningItems() {
			if (State.warningItems.get() != null) {
				State.warningItems.get().clear();
			}
		}

		public static boolean addSuccessful(WsIOItem element) {
			if (State.successfulItems.get() == null) {
				State.successfulItems.set(new ArrayList<>());
			}
			return State.successfulItems.get().add(element);
		}

		public static boolean removeSuccessful(WsIOItem element) {
			if (State.successfulItems.get() != null) {
				return State.successfulItems.get().remove(element);
			}
			return false;
		}

		public static boolean addFailure(WsIOItem element) {
			if (State.failureItems.get() == null) {
				State.failureItems.set(new ArrayList<>());
			}
			return State.failureItems.get().add(element);
		}

		public static boolean removeFailure(WsIOItem element) {
			if (State.failureItems.get() != null) {
				return State.failureItems.get().remove(element);
			}
			return false;
		}

		public static boolean addWarning(WsIOItem element) {
			if (State.warningItems.get() == null) {
				State.warningItems.set(new ArrayList<>());
			}
			return State.warningItems.get().add(element);
		}

		public static boolean removeWarning(WsIOItem element) {
			if (State.warningItems.get() != null) {
				return State.warningItems.get().remove(element);
			}
			return false;
		}

		public static boolean addSuccessfulOf(String identifier,
		                                      WsIOText message,
		                                      WsIOText description,
		                                      String type) {
			return addSuccessful(WsIOItem.of(identifier, message, description, type));
		}

		public static boolean removeSuccessfulOf(String identifier,
		                                         WsIOText message,
		                                         WsIOText description,
		                                         String type) {
			return removeSuccessful(WsIOItem.of(identifier, message, description, type));
		}

		public static boolean addFailureOf(String identifier,
		                                   WsIOText message,
		                                   WsIOText description,
		                                   String type) {
			return addFailure(WsIOItem.of(identifier, message, description, type));
		}

		public static boolean removeFailureOf(String identifier,
		                                      WsIOText message,
		                                      WsIOText description,
		                                      String type) {
			return removeFailure(WsIOItem.of(identifier, message, description, type));
		}

		public static boolean addWarningOf(String identifier,
		                                   WsIOText message,
		                                   WsIOText description,
		                                   String type) {
			return addWarning(WsIOItem.of(identifier, message, description, type));
		}

		public static boolean removeWarningOf(String identifier,
		                                      WsIOText message,
		                                      WsIOText description,
		                                      String type) {
			return removeWarning(WsIOItem.of(identifier, message, description, type));
		}

		public static boolean addSuccessfulId(String identifier) {
			return addSuccessful(WsIOItem.id(identifier));
		}

		public static boolean removeSuccessfulId(String identifier) {
			return removeSuccessful(WsIOItem.id(identifier));
		}

		public static boolean addFailureId(String identifier) {
			return addFailure(WsIOItem.id(identifier));
		}

		public static boolean removeFailureId(String identifier) {
			return removeFailure(WsIOItem.id(identifier));
		}

		public static boolean addWarningId(String identifier) {
			return addWarning(WsIOItem.id(identifier));
		}

		public static boolean removeWarningId(String identifier) {
			return removeWarning(WsIOItem.id(identifier));
		}

		public static boolean addSuccessfulMessage(WsIOText message) {
			return addSuccessful(WsIOItem.message(message));
		}

		public static boolean removeSuccessfulMessage(WsIOText message) {
			return removeSuccessful(WsIOItem.message(message));
		}

		public static boolean addFailureMessage(WsIOText message) {
			return addFailure(WsIOItem.message(message));
		}

		public static boolean removeFailureMessage(WsIOText message) {
			return removeFailure(WsIOItem.message(message));
		}

		public static boolean addWarningMessage(WsIOText message) {
			return addWarning(WsIOItem.message(message));
		}

		public static boolean removeWarningMessage(WsIOText message) {
			return removeWarning(WsIOItem.message(message));
		}

		public static boolean addSuccessfulDescription(WsIOText description) {
			return addSuccessful(WsIOItem.description(description));
		}

		public static boolean removeSuccessfulDescription(WsIOText description) {
			return removeSuccessful(WsIOItem.description(description));
		}

		public static boolean addFailureDescription(WsIOText description) {
			return addFailure(WsIOItem.description(description));
		}

		public static boolean removeFailureDescription(WsIOText description) {
			return removeFailure(WsIOItem.description(description));
		}

		public static boolean addWarningDescription(WsIOText description) {
			return addWarning(WsIOItem.description(description));
		}

		public static boolean removeWarningDescription(WsIOText description) {
			return removeWarning(WsIOItem.description(description));
		}

		public static boolean addSuccessfulType(String type) {
			return addSuccessful(WsIOItem.type(type));
		}

		public static boolean removeSuccessfulType(String type) {
			return removeSuccessful(WsIOItem.type(type));
		}

		public static boolean addFailureType(String type) {
			return addFailure(WsIOItem.type(type));
		}

		public static boolean removeFailureType(String type) {
			return removeFailure(WsIOItem.type(type));
		}

		public static boolean addWarningType(String type) {
			return addWarning(WsIOItem.type(type));
		}

		public static boolean removeWarningType(String type) {
			return removeWarning(WsIOItem.type(type));
		}

		public static boolean addSuccessfulMessageOf(WsIOLanguage language, String message) {
			return addSuccessful(WsIOItem.messageOf(language, message));
		}

		public static boolean removeSuccessfulMessageOf(WsIOLanguage language, String message) {
			return removeSuccessful(WsIOItem.messageOf(language, message));
		}

		public static boolean addFailureMessageOf(WsIOLanguage language, String message) {
			return addFailure(WsIOItem.messageOf(language, message));
		}

		public static boolean removeFailureMessageOf(WsIOLanguage language, String message) {
			return removeFailure(WsIOItem.messageOf(language, message));
		}

		public static boolean addWarningMessageOf(WsIOLanguage language, String message) {
			return addWarning(WsIOItem.messageOf(language, message));
		}

		public static boolean removeWarningMessageOf(WsIOLanguage language, String message) {
			return removeWarning(WsIOItem.messageOf(language, message));
		}

		public static boolean addSuccessfulDescriptionOf(WsIOLanguage language, String description) {
			return addSuccessful(WsIOItem.descriptionOf(language, description));
		}

		public static boolean removeSuccessfulDescriptionOf(WsIOLanguage language, String description) {
			return removeSuccessful(WsIOItem.descriptionOf(language, description));
		}

		public static boolean addFailureDescriptionOf(WsIOLanguage language, String description) {
			return addFailure(WsIOItem.descriptionOf(language, description));
		}

		public static boolean removeFailureDescriptionOf(WsIOLanguage language, String description) {
			return removeFailure(WsIOItem.descriptionOf(language, description));
		}

		public static boolean addWarningDescriptionOf(WsIOLanguage language, String description) {
			return addWarning(WsIOItem.descriptionOf(language, description));
		}

		public static boolean removeWarningDescriptionOf(WsIOLanguage language, String description) {
			return removeWarning(WsIOItem.descriptionOf(language, description));
		}

		public static boolean addSuccessfulMessageDef(String message) {
			return addSuccessful(WsIOItem.messageDef(message));
		}

		public static boolean removeSuccessfulMessageDef(String message) {
			return removeSuccessful(WsIOItem.messageDef(message));
		}

		public static boolean addFailureMessageDef(String message) {
			return addFailure(WsIOItem.messageDef(message));
		}

		public static boolean removeFailureMessageDef(String message) {
			return removeFailure(WsIOItem.messageDef(message));
		}

		public static boolean addWarningMessageDef(String message) {
			return addWarning(WsIOItem.messageDef(message));
		}

		public static boolean removeWarningMessageDef(String message) {
			return removeWarning(WsIOItem.messageDef(message));
		}

		public static boolean addSuccessfulDescriptionDef(String description) {
			return addSuccessful(WsIOItem.descriptionDef(description));
		}

		public static boolean removeSuccessfulDescriptionDef(String description) {
			return removeSuccessful(WsIOItem.descriptionDef(description));
		}

		public static boolean addFailureDescriptionDef(String description) {
			return addFailure(WsIOItem.descriptionDef(description));
		}

		public static boolean removeFailureDescriptionDef(String description) {
			return removeFailure(WsIOItem.descriptionDef(description));
		}

		public static boolean addWarningDescriptionDef(String description) {
			return addWarning(WsIOItem.descriptionDef(description));
		}

		public static boolean removeWarningDescriptionDef(String description) {
			return removeWarning(WsIOItem.descriptionDef(description));
		}

		public static boolean addSuccessfulMessageNolang(String message) {
			return addSuccessful(WsIOItem.messageNoLang(message));
		}

		public static boolean removeSuccessfulMessageNolang(String message) {
			return removeSuccessful(WsIOItem.messageNoLang(message));
		}

		public static boolean addFailureMessageNolang(String message) {
			return addFailure(WsIOItem.messageNoLang(message));
		}

		public static boolean removeFailureMessageNolang(String message) {
			return removeFailure(WsIOItem.messageNoLang(message));
		}

		public static boolean addWarningMessageNolang(String message) {
			return addWarning(WsIOItem.messageNoLang(message));
		}

		public static boolean removeWarningMessageNolang(String message) {
			return removeWarning(WsIOItem.messageNoLang(message));
		}

		public static boolean addSuccessfulDescriptionNolang(String description) {
			return addSuccessful(WsIOItem.descriptionNoLang(description));
		}

		public static boolean removeSuccessfulDescriptionNolang(String description) {
			return removeSuccessful(WsIOItem.descriptionNoLang(description));
		}

		public static boolean addFailureDescriptionNolang(String description) {
			return addFailure(WsIOItem.descriptionNoLang(description));
		}

		public static boolean removeFailureDescriptionNolang(String description) {
			return removeFailure(WsIOItem.descriptionNoLang(description));
		}

		public static boolean addWarningDescriptionNolang(String description) {
			return addWarning(WsIOItem.descriptionNoLang(description));
		}

		public static boolean removeWarningDescriptionNolang(String description) {
			return removeWarning(WsIOItem.descriptionNoLang(description));
		}

		public static boolean addSuccessfulBasic(String identifier,
		                                         WsIOText message) {
			return addSuccessful(WsIOItem.basic(identifier, message));
		}

		public static boolean removeSuccessfulBasic(String identifier,
		                                            WsIOText message) {
			return removeSuccessful(WsIOItem.basic(identifier, message));
		}

		public static boolean addFailureBasic(String identifier,
		                                      WsIOText message) {
			return addFailure(WsIOItem.basic(identifier, message));
		}

		public static boolean removeFailureBasic(String identifier,
		                                         WsIOText message) {
			return removeFailure(WsIOItem.basic(identifier, message));
		}

		public static boolean addWarningBasic(String identifier,
		                                      WsIOText message) {
			return addWarning(WsIOItem.basic(identifier, message));
		}

		public static boolean removeWarningBasic(String identifier,
		                                         WsIOText message) {
			return removeWarning(WsIOItem.basic(identifier, message));
		}

		public static boolean addSuccessfulSimple(WsIOText message,
		                                          String type) {
			return addSuccessful(WsIOItem.simple(message, type));
		}

		public static boolean removeSuccessfulSimple(WsIOText message,
		                                             String type) {
			return removeSuccessful(WsIOItem.simple(message, type));
		}

		public static boolean addFailureSimple(WsIOText message,
		                                       String type) {
			return addFailure(WsIOItem.simple(message, type));
		}

		public static boolean removeFailureSimple(WsIOText message,
		                                          String type) {
			return removeFailure(WsIOItem.simple(message, type));
		}

		public static boolean addWarningSimple(WsIOText message,
		                                       String type) {
			return addWarning(WsIOItem.simple(message, type));
		}

		public static boolean removeWarningSimple(WsIOText message,
		                                          String type) {
			return removeWarning(WsIOItem.simple(message, type));
		}

		public static boolean addSuccessfulText(WsIOText message,
		                                        WsIOText description) {
			return addSuccessful(WsIOItem.text(message, description));
		}

		public static boolean removeSuccessfulText(WsIOText message,
		                                           WsIOText description) {
			return removeSuccessful(WsIOItem.text(message, description));
		}

		public static boolean addFailureText(WsIOText message,
		                                     WsIOText description) {
			return addFailure(WsIOItem.text(message, description));
		}

		public static boolean removeFailureText(WsIOText message,
		                                        WsIOText description) {
			return removeFailure(WsIOItem.text(message, description));
		}

		public static boolean addWarningText(WsIOText message,
		                                     WsIOText description) {
			return addWarning(WsIOItem.text(message, description));
		}

		public static boolean removeWarningText(WsIOText message,
		                                        WsIOText description) {
			return removeWarning(WsIOItem.text(message, description));
		}

		public static boolean addSuccessfulTyped(String identifier,
		                                         String type) {
			return addSuccessful(WsIOItem.typed(identifier, type));
		}

		public static boolean removeSuccessfulTyped(String identifier,
		                                            String type) {
			return removeSuccessful(WsIOItem.typed(identifier, type));
		}

		public static boolean addFailureTyped(String identifier,
		                                      String type) {
			return addFailure(WsIOItem.typed(identifier, type));
		}

		public static boolean removeFailureTyped(String identifier,
		                                         String type) {
			return removeFailure(WsIOItem.typed(identifier, type));
		}

		public static boolean addWarningTyped(String identifier,
		                                      String type) {
			return addWarning(WsIOItem.typed(identifier, type));
		}

		public static boolean removeWarningTyped(String identifier,
		                                         String type) {
			return removeWarning(WsIOItem.typed(identifier, type));
		}

		public static boolean addSuccessfulNotype(String identifier,
		                                          WsIOText message,
		                                          WsIOText description) {
			return addSuccessful(WsIOItem.noType(identifier, message, description));
		}

		public static boolean removeSuccessfulNotype(String identifier,
		                                             WsIOText message,
		                                             WsIOText description) {
			return removeSuccessful(WsIOItem.noType(identifier, message, description));
		}

		public static boolean addFailureNotype(String identifier,
		                                       WsIOText message,
		                                       WsIOText description) {
			return addFailure(WsIOItem.noType(identifier, message, description));
		}

		public static boolean removeFailureNotype(String identifier,
		                                          WsIOText message,
		                                          WsIOText description) {
			return removeFailure(WsIOItem.noType(identifier, message, description));
		}

		public static boolean addWarningNotype(String identifier,
		                                       WsIOText message,
		                                       WsIOText description) {
			return addWarning(WsIOItem.noType(identifier, message, description));
		}

		public static boolean removeWarningNotype(String identifier,
		                                          WsIOText message,
		                                          WsIOText description) {
			return removeWarning(WsIOItem.noType(identifier, message, description));
		}

		public static boolean addSuccessfulNodesc(String identifier,
		                                          WsIOText message,
		                                          String type) {
			return addSuccessful(WsIOItem.noDesc(identifier, message, type));
		}

		public static boolean removeSuccessfulNodesc(String identifier,
		                                             WsIOText message,
		                                             String type) {
			return removeSuccessful(WsIOItem.noDesc(identifier, message, type));
		}

		public static boolean addFailureNodesc(String identifier,
		                                       WsIOText message,
		                                       String type) {
			return addFailure(WsIOItem.noDesc(identifier, message, type));
		}

		public static boolean removeFailureNodesc(String identifier,
		                                          WsIOText message,
		                                          String type) {
			return removeFailure(WsIOItem.noDesc(identifier, message, type));
		}

		public static boolean addWarningNodesc(String identifier,
		                                       WsIOText message,
		                                       String type) {
			return addWarning(WsIOItem.noDesc(identifier, message, type));
		}

		public static boolean removeWarningNodesc(String identifier,
		                                          WsIOText message,
		                                          String type) {
			return removeWarning(WsIOItem.noDesc(identifier, message, type));
		}

		public static boolean addSuccessfulNoid(WsIOText message,
		                                        WsIOText description,
		                                        String type) {
			return addSuccessful(WsIOItem.noId(message, description, type));
		}

		public static boolean removeSuccessfulNoid(WsIOText message,
		                                           WsIOText description,
		                                           String type) {
			return removeSuccessful(WsIOItem.noId(message, description, type));
		}

		public static boolean addFailureNoid(WsIOText message,
		                                     WsIOText description,
		                                     String type) {
			return addFailure(WsIOItem.noId(message, description, type));
		}

		public static boolean removeFailureNoid(WsIOText message,
		                                        WsIOText description,
		                                        String type) {
			return removeFailure(WsIOItem.noId(message, description, type));
		}

		public static boolean addWarningNoid(WsIOText message,
		                                     WsIOText description,
		                                     String type) {
			return addWarning(WsIOItem.noId(message, description, type));
		}

		public static boolean removeWarningNoid(WsIOText message,
		                                        WsIOText description,
		                                        String type) {
			return removeWarning(WsIOItem.noId(message, description, type));
		}

		public static boolean addSuccessfulBasicOf(String identifier,
		                                           WsIOLanguage language,
		                                           String message) {
			return addSuccessful(WsIOItem.basicOf(identifier, language, message));
		}

		public static boolean removeSuccessfulBasicOf(String identifier,
		                                              WsIOLanguage language,
		                                              String message) {
			return removeSuccessful(WsIOItem.basicOf(identifier, language, message));
		}

		public static boolean addFailureBasicOf(String identifier,
		                                        WsIOLanguage language,
		                                        String message) {
			return addFailure(WsIOItem.basicOf(identifier, language, message));
		}

		public static boolean removeFailureBasicOf(String identifier,
		                                           WsIOLanguage language,
		                                           String message) {
			return removeFailure(WsIOItem.basicOf(identifier, language, message));
		}

		public static boolean addWarningBasicOf(String identifier,
		                                        WsIOLanguage language,
		                                        String message) {
			return addWarning(WsIOItem.basicOf(identifier, language, message));
		}

		public static boolean removeWarningBasicOf(String identifier,
		                                           WsIOLanguage language,
		                                           String message) {
			return removeWarning(WsIOItem.basicOf(identifier, language, message));
		}

		public static boolean addSuccessfulSimpleOf(WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return addSuccessful(WsIOItem.simpleOf(language, message, type));
		}

		public static boolean removeSuccessfulSimpleOf(WsIOLanguage language,
		                                               String message,
		                                               String type) {
			return removeSuccessful(WsIOItem.simpleOf(language, message, type));
		}

		public static boolean addFailureSimpleOf(WsIOLanguage language,
		                                         String message,
		                                         String type) {
			return addFailure(WsIOItem.simpleOf(language, message, type));
		}

		public static boolean removeFailureSimpleOf(WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return removeFailure(WsIOItem.simpleOf(language, message, type));
		}

		public static boolean addWarningSimpleOf(WsIOLanguage language,
		                                         String message,
		                                         String type) {
			return addWarning(WsIOItem.simpleOf(language, message, type));
		}

		public static boolean removeWarningSimpleOf(WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return removeWarning(WsIOItem.simpleOf(language, message, type));
		}

		public static boolean addSuccessfulTextOf(WsIOLanguage language,
		                                          String message,
		                                          String description) {
			return addSuccessful(WsIOItem.textOf(language, message, description));
		}

		public static boolean removeSuccessfulTextOf(WsIOLanguage language,
		                                             String message,
		                                             String description) {
			return removeSuccessful(WsIOItem.textOf(language, message, description));
		}

		public static boolean addFailureTextOf(WsIOLanguage language,
		                                       String message,
		                                       String description) {
			return addFailure(WsIOItem.textOf(language, message, description));
		}

		public static boolean removeFailureTextOf(WsIOLanguage language,
		                                          String message,
		                                          String description) {
			return removeFailure(WsIOItem.textOf(language, message, description));
		}

		public static boolean addWarningTextOf(WsIOLanguage language,
		                                       String message,
		                                       String description) {
			return addWarning(WsIOItem.textOf(language, message, description));
		}

		public static boolean removeWarningTextOf(WsIOLanguage language,
		                                          String message,
		                                          String description) {
			return removeWarning(WsIOItem.textOf(language, message, description));
		}

		public static boolean addSuccessfulBasicDef(String identifier,
		                                            String message) {
			return addSuccessful(WsIOItem.basicDef(identifier, message));
		}

		public static boolean removeSuccessfulBasicDef(String identifier,
		                                               String message) {
			return removeSuccessful(WsIOItem.basicDef(identifier, message));
		}

		public static boolean addFailureBasicDef(String identifier,
		                                         String message) {
			return addFailure(WsIOItem.basicDef(identifier, message));
		}

		public static boolean removeFailureBasicDef(String identifier,
		                                            String message) {
			return removeFailure(WsIOItem.basicDef(identifier, message));
		}

		public static boolean addWarningBasicDef(String identifier,
		                                         String message) {
			return addWarning(WsIOItem.basicDef(identifier, message));
		}

		public static boolean removeWarningBasicDef(String identifier,
		                                            String message) {
			return removeWarning(WsIOItem.basicDef(identifier, message));
		}

		public static boolean addSuccessfulSimpleDef(String message,
		                                             String type) {
			return addSuccessful(WsIOItem.simpleDef(message, type));
		}

		public static boolean removeSuccessfulSimpleDef(String message,
		                                                String type) {
			return removeSuccessful(WsIOItem.simpleDef(message, type));
		}

		public static boolean addFailureSimpleDef(String message,
		                                          String type) {
			return addFailure(WsIOItem.simpleDef(message, type));
		}

		public static boolean removeFailureSimpleDef(String message,
		                                             String type) {
			return removeFailure(WsIOItem.simpleDef(message, type));
		}

		public static boolean addWarningSimpleDef(String message,
		                                          String type) {
			return addWarning(WsIOItem.simpleDef(message, type));
		}

		public static boolean removeWarningSimpleDef(String message,
		                                             String type) {
			return removeWarning(WsIOItem.simpleDef(message, type));
		}

		public static boolean addSuccessfulTextDef(String message,
		                                           String description) {
			return addSuccessful(WsIOItem.textDef(message, description));
		}

		public static boolean removeSuccessfulTextDef(String message,
		                                              String description) {
			return removeSuccessful(WsIOItem.textDef(message, description));
		}

		public static boolean addFailureTextDef(String message,
		                                        String description) {
			return addFailure(WsIOItem.textDef(message, description));
		}

		public static boolean removeFailureTextDef(String message,
		                                           String description) {
			return removeFailure(WsIOItem.textDef(message, description));
		}

		public static boolean addWarningTextDef(String message,
		                                        String description) {
			return addWarning(WsIOItem.textDef(message, description));
		}

		public static boolean removeWarningTextDef(String message,
		                                           String description) {
			return removeWarning(WsIOItem.textDef(message, description));
		}

		public static boolean addSuccessfulBasicNolang(String identifier,
		                                               String message) {
			return addSuccessful(WsIOItem.basicNoLang(identifier, message));
		}

		public static boolean removeSuccessfulBasicNolang(String identifier,
		                                                  String message) {
			return removeSuccessful(WsIOItem.basicNoLang(identifier, message));
		}

		public static boolean addFailureBasicNolang(String identifier,
		                                            String message) {
			return addFailure(WsIOItem.basicNoLang(identifier, message));
		}

		public static boolean removeFailureBasicNolang(String identifier,
		                                               String message) {
			return removeFailure(WsIOItem.basicNoLang(identifier, message));
		}

		public static boolean addWarningBasicNolang(String identifier,
		                                            String message) {
			return addWarning(WsIOItem.basicNoLang(identifier, message));
		}

		public static boolean removeWarningBasicNolang(String identifier,
		                                               String message) {
			return removeWarning(WsIOItem.basicNoLang(identifier, message));
		}

		public static boolean addSuccessfulSimpleNolang(String message,
		                                                String type) {
			return addSuccessful(WsIOItem.simpleNoLang(message, type));
		}

		public static boolean removeSuccessfulSimpleNolang(String message,
		                                                   String type) {
			return removeSuccessful(WsIOItem.simpleNoLang(message, type));
		}

		public static boolean addFailureSimpleNolang(String message,
		                                             String type) {
			return addFailure(WsIOItem.simpleNoLang(message, type));
		}

		public static boolean removeFailureSimpleNolang(String message,
		                                                String type) {
			return removeFailure(WsIOItem.simpleNoLang(message, type));
		}

		public static boolean addWarningSimpleNolang(String message,
		                                             String type) {
			return addWarning(WsIOItem.simpleNoLang(message, type));
		}

		public static boolean removeWarningSimpleNolang(String message,
		                                                String type) {
			return removeWarning(WsIOItem.simpleNoLang(message, type));
		}

		public static boolean addSuccessfulTextNolang(String message,
		                                              String description) {
			return addSuccessful(WsIOItem.textNoLang(message, description));
		}

		public static boolean removeSuccessfulTextNolang(String message,
		                                                 String description) {
			return removeSuccessful(WsIOItem.textNoLang(message, description));
		}

		public static boolean addFailureTextNolang(String message,
		                                           String description) {
			return addFailure(WsIOItem.textNoLang(message, description));
		}

		public static boolean removeFailureTextNolang(String message,
		                                              String description) {
			return removeFailure(WsIOItem.textNoLang(message, description));
		}

		public static boolean addWarningTextNolang(String message,
		                                           String description) {
			return addWarning(WsIOItem.textNoLang(message, description));
		}

		public static boolean removeWarningTextNolang(String message,
		                                              String description) {
			return removeWarning(WsIOItem.textNoLang(message, description));
		}

		public static boolean addSuccessfulNotypeOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String description) {
			return addSuccessful(WsIOItem.noTypeOf(identifier, language, message, description));
		}

		public static boolean removeSuccessfulNotypeOf(String identifier,
		                                               WsIOLanguage language,
		                                               String message,
		                                               String description) {
			return removeSuccessful(WsIOItem.noTypeOf(identifier, language, message, description));
		}

		public static boolean addFailureNotypeOf(String identifier,
		                                         WsIOLanguage language,
		                                         String message,
		                                         String description) {
			return addFailure(WsIOItem.noTypeOf(identifier, language, message, description));
		}

		public static boolean removeFailureNotypeOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String description) {
			return removeFailure(WsIOItem.noTypeOf(identifier, language, message, description));
		}

		public static boolean addWarningNotypeOf(String identifier,
		                                         WsIOLanguage language,
		                                         String message,
		                                         String description) {
			return addWarning(WsIOItem.noTypeOf(identifier, language, message, description));
		}

		public static boolean removeWarningNotypeOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String description) {
			return removeWarning(WsIOItem.noTypeOf(identifier, language, message, description));
		}

		public static boolean addSuccessfulNodescOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return addSuccessful(WsIOItem.noDescOf(identifier, language, message, type));
		}

		public static boolean removeSuccessfulNodescOf(String identifier,
		                                               WsIOLanguage language,
		                                               String message,
		                                               String type) {
			return removeSuccessful(WsIOItem.noDescOf(identifier, language, message, type));
		}

		public static boolean addFailureNodescOf(String identifier,
		                                         WsIOLanguage language,
		                                         String message,
		                                         String type) {
			return addFailure(WsIOItem.noDescOf(identifier, language, message, type));
		}

		public static boolean removeFailureNodescOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return removeFailure(WsIOItem.noDescOf(identifier, language, message, type));
		}

		public static boolean addWarningNodescOf(String identifier,
		                                         WsIOLanguage language,
		                                         String message,
		                                         String type) {
			return addWarning(WsIOItem.noDescOf(identifier, language, message, type));
		}

		public static boolean removeWarningNodescOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return removeWarning(WsIOItem.noDescOf(identifier, language, message, type));
		}

		public static boolean addSuccessfulNoidOf(WsIOLanguage language,
		                                          String message,
		                                          String description,
		                                          String type) {
			return addSuccessful(WsIOItem.noIdOf(language, message, description, type));
		}

		public static boolean removeSuccessfulNoidOf(WsIOLanguage language,
		                                             String message,
		                                             String description,
		                                             String type) {
			return removeSuccessful(WsIOItem.noIdOf(language, message, description, type));
		}

		public static boolean addFailureNoidOf(WsIOLanguage language,
		                                       String message,
		                                       String description,
		                                       String type) {
			return addFailure(WsIOItem.noIdOf(language, message, description, type));
		}

		public static boolean removeFailureNoidOf(WsIOLanguage language,
		                                          String message,
		                                          String description,
		                                          String type) {
			return removeFailure(WsIOItem.noIdOf(language, message, description, type));
		}

		public static boolean addWarningNoidOf(WsIOLanguage language,
		                                       String message,
		                                       String description,
		                                       String type) {
			return addWarning(WsIOItem.noIdOf(language, message, description, type));
		}

		public static boolean removeWarningNoidOf(WsIOLanguage language,
		                                          String message,
		                                          String description,
		                                          String type) {
			return removeWarning(WsIOItem.noIdOf(language, message, description, type));
		}

		public static boolean addSuccessfulNotypeDef(String identifier,
		                                             String message,
		                                             String description) {
			return addSuccessful(WsIOItem.noTypeDef(identifier, message, description));
		}

		public static boolean removeSuccessfulNotypeDef(String identifier,
		                                                String message,
		                                                String description) {
			return removeSuccessful(WsIOItem.noTypeDef(identifier, message, description));
		}

		public static boolean addFailureNotypeDef(String identifier,
		                                          String message,
		                                          String description) {
			return addFailure(WsIOItem.noTypeDef(identifier, message, description));
		}

		public static boolean removeFailureNotypeDef(String identifier,
		                                             String message,
		                                             String description) {
			return removeFailure(WsIOItem.noTypeDef(identifier, message, description));
		}

		public static boolean addWarningNotypeDef(String identifier,
		                                          String message,
		                                          String description) {
			return addWarning(WsIOItem.noTypeDef(identifier, message, description));
		}

		public static boolean removeWarningNotypeDef(String identifier,
		                                             String message,
		                                             String description) {
			return removeWarning(WsIOItem.noTypeDef(identifier, message, description));
		}

		public static boolean addSuccessfulNodescDef(String identifier,
		                                             String message,
		                                             String type) {
			return addSuccessful(WsIOItem.noDescDef(identifier, message, type));
		}

		public static boolean removeSuccessfulNodescDef(String identifier,
		                                                String message,
		                                                String type) {
			return removeSuccessful(WsIOItem.noDescDef(identifier, message, type));
		}

		public static boolean addFailureNodescDef(String identifier,
		                                          String message,
		                                          String type) {
			return addFailure(WsIOItem.noDescDef(identifier, message, type));
		}

		public static boolean removeFailureNodescDef(String identifier,
		                                             String message,
		                                             String type) {
			return removeFailure(WsIOItem.noDescDef(identifier, message, type));
		}

		public static boolean addWarningNodescDef(String identifier,
		                                          String message,
		                                          String type) {
			return addWarning(WsIOItem.noDescDef(identifier, message, type));
		}

		public static boolean removeWarningNodescDef(String identifier,
		                                             String message,
		                                             String type) {
			return removeWarning(WsIOItem.noDescDef(identifier, message, type));
		}

		public static boolean addSuccessfulNoidDef(String message,
		                                           String description,
		                                           String type) {
			return addSuccessful(WsIOItem.noIdDef(message, description, type));
		}

		public static boolean removeSuccessfulNoidDef(String message,
		                                              String description,
		                                              String type) {
			return removeSuccessful(WsIOItem.noIdDef(message, description, type));
		}

		public static boolean addFailureNoidDef(String message,
		                                        String description,
		                                        String type) {
			return addFailure(WsIOItem.noIdDef(message, description, type));
		}

		public static boolean removeFailureNoidDef(String message,
		                                           String description,
		                                           String type) {
			return removeFailure(WsIOItem.noIdDef(message, description, type));
		}

		public static boolean addWarningNoidDef(String message,
		                                        String description,
		                                        String type) {
			return addWarning(WsIOItem.noIdDef(message, description, type));
		}

		public static boolean removeWarningNoidDef(String message,
		                                           String description,
		                                           String type) {
			return removeWarning(WsIOItem.noIdDef(message, description, type));
		}

		public static boolean addSuccessfulNotypeNolang(String identifier,
		                                                String message,
		                                                String description) {
			return addSuccessful(WsIOItem.noTypeNoLang(identifier, message, description));
		}

		public static boolean removeSuccessfulNotypeNolang(String identifier,
		                                                   String message,
		                                                   String description) {
			return removeSuccessful(WsIOItem.noTypeNoLang(identifier, message, description));
		}

		public static boolean addFailureNotypeNolang(String identifier,
		                                             String message,
		                                             String description) {
			return addFailure(WsIOItem.noTypeNoLang(identifier, message, description));
		}

		public static boolean removeFailureNotypeNolang(String identifier,
		                                                String message,
		                                                String description) {
			return removeFailure(WsIOItem.noTypeNoLang(identifier, message, description));
		}

		public static boolean addWarningNotypeNolang(String identifier,
		                                             String message,
		                                             String description) {
			return addWarning(WsIOItem.noTypeNoLang(identifier, message, description));
		}

		public static boolean removeWarningNotypeNolang(String identifier,
		                                                String message,
		                                                String description) {
			return removeWarning(WsIOItem.noTypeNoLang(identifier, message, description));
		}

		public static boolean addSuccessfulNodescNolang(String identifier,
		                                                String message,
		                                                String type) {
			return addSuccessful(WsIOItem.noDescNoLang(identifier, message, type));
		}

		public static boolean removeSuccessfulNodescNolang(String identifier,
		                                                   String message,
		                                                   String type) {
			return removeSuccessful(WsIOItem.noDescNoLang(identifier, message, type));
		}

		public static boolean addFailureNodescNolang(String identifier,
		                                             String message,
		                                             String type) {
			return addFailure(WsIOItem.noDescNoLang(identifier, message, type));
		}

		public static boolean removeFailureNodescNolang(String identifier,
		                                                String message,
		                                                String type) {
			return removeFailure(WsIOItem.noDescNoLang(identifier, message, type));
		}

		public static boolean addWarningNodescNolang(String identifier,
		                                             String message,
		                                             String type) {
			return addWarning(WsIOItem.noDescNoLang(identifier, message, type));
		}

		public static boolean removeWarningNodescNolang(String identifier,
		                                                String message,
		                                                String type) {
			return removeWarning(WsIOItem.noDescNoLang(identifier, message, type));
		}

		public static boolean addSuccessfulNoidNolang(String message,
		                                              String description,
		                                              String type) {
			return addSuccessful(WsIOItem.noIdNoLang(message, description, type));
		}

		public static boolean removeSuccessfulNoidNolang(String message,
		                                                 String description,
		                                                 String type) {
			return removeSuccessful(WsIOItem.noIdNoLang(message, description, type));
		}

		public static boolean addFailureNoidNolang(String message,
		                                           String description,
		                                           String type) {
			return addFailure(WsIOItem.noIdNoLang(message, description, type));
		}

		public static boolean removeFailureNoidNolang(String message,
		                                              String description,
		                                              String type) {
			return removeFailure(WsIOItem.noIdNoLang(message, description, type));
		}

		public static boolean addWarningNoidNolang(String message,
		                                           String description,
		                                           String type) {
			return addWarning(WsIOItem.noIdNoLang(message, description, type));
		}

		public static boolean removeWarningNoidNolang(String message,
		                                              String description,
		                                              String type) {
			return removeWarning(WsIOItem.noIdNoLang(message, description, type));
		}

		public static void setMessageOf(WsIOLanguage language, String message) {
			State.message.set(WsIOText.of(language, message));
		}

		public static void setDescriptionOf(WsIOLanguage language, String description) {
			State.description.set(WsIOText.of(language, description));
		}

		public static void setMessageNolang(String message) {
			State.message.set(WsIOText.noLang(message));
		}

		public static void setDescriptionNolang(String description) {
			State.description.set(WsIOText.noLang(description));
		}

	}

}