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

		private static final ThreadLocal<List<WsIOElement>> successfuls = new ThreadLocal<>();

		private static final ThreadLocal<List<WsIOElement>> failures = new ThreadLocal<>();

		private static final ThreadLocal<List<WsIOElement>> warnings = new ThreadLocal<>();

		private static final ThreadLocal<Boolean> showSuccessfuls = new ThreadLocal<>();

		private static final ThreadLocal<Boolean> showFailures = new ThreadLocal<>();

		private static final ThreadLocal<Boolean> showWarnings = new ThreadLocal<>();

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
				if (successfuls.get() != null) {
					stateWrapper.setSuccessfuls(successfuls.get());
				}
				if (failures.get() != null) {
					stateWrapper.setFailures(failures.get());
				}
				if (warnings.get() != null) {
					stateWrapper.setWarnings(warnings.get());
				}
				if (showSuccessfuls.get() != null) {
					stateWrapper.setShowSuccessfuls(showSuccessfuls.get());
				}
				if (showFailures.get() != null) {
					stateWrapper.setShowFailures(showFailures.get());
				}
				if (showWarnings.get() != null) {
					stateWrapper.setShowWarnings(showWarnings.get());
				}
			}
		}

		public static void clear() {
			clearSuccessfuls();
			clearFailures();
			clearWarnings();
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
			resetSuccessfuls();
			resetFailures();
			resetWarnings();
			resetShowSuccessfuls();
			resetShowFailures();
			resetShowWarnings();
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

		public static List<WsIOElement> getSuccessfuls() {
			return successfuls.get();
		}

		public static void setSuccessfuls(List<WsIOElement> successfuls) {
			State.successfuls.set(successfuls);
		}

		public static List<WsIOElement> getFailures() {
			return failures.get();
		}

		public static void setFailures(List<WsIOElement> failures) {
			State.failures.set(failures);
		}

		public static List<WsIOElement> getWarnings() {
			return warnings.get();
		}

		public static void setWarnings(List<WsIOElement> warnings) {
			State.warnings.set(warnings);
		}

		public static Boolean getShowSuccessfuls() {
			return showSuccessfuls.get();
		}

		public static void setShowSuccessfuls(Boolean showSuccessfuls) {
			State.showSuccessfuls.set(showSuccessfuls);
		}

		public static Boolean getShowFailures() {
			return showFailures.get();
		}

		public static void setShowFailures(Boolean showFailures) {
			State.showFailures.set(showFailures);
		}

		public static Boolean getShowWarnings() {
			return showWarnings.get();
		}

		public static void setShowWarnings(Boolean showWarnings) {
			State.showWarnings.set(showWarnings);
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

		public static void resetSuccessfuls() {
			State.successfuls.set(null);
		}

		public static void resetFailures() {
			State.failures.set(null);
		}

		public static void resetWarnings() {
			State.warnings.set(null);
		}

		public static void resetShowSuccessfuls() {
			State.showSuccessfuls.set(null);
		}

		public static void resetShowFailures() {
			State.showFailures.set(null);
		}

		public static void resetShowWarnings() {
			State.showWarnings.set(null);
		}

		public static void resetDefaultLanguage() {
			State.defaultLanguage.set(null);
		}

		public static void clearSuccessfuls() {
			if (State.successfuls.get() != null) {
				State.successfuls.get().clear();
			}
		}

		public static void clearFailures() {
			if (State.failures.get() != null) {
				State.failures.get().clear();
			}
		}

		public static void clearWarnings() {
			if (State.warnings.get() != null) {
				State.warnings.get().clear();
			}
		}

		public static boolean addSuccessful(WsIOElement element) {
			if (State.successfuls.get() == null) {
				State.successfuls.set(new ArrayList<>());
			}
			return State.successfuls.get().add(element);
		}

		public static boolean removeSuccessful(WsIOElement element) {
			if (State.successfuls.get() != null) {
				return State.successfuls.get().remove(element);
			}
			return false;
		}

		public static boolean addFailure(WsIOElement element) {
			if (State.failures.get() == null) {
				State.failures.set(new ArrayList<>());
			}
			return State.failures.get().add(element);
		}

		public static boolean removeFailure(WsIOElement element) {
			if (State.failures.get() != null) {
				return State.failures.get().remove(element);
			}
			return false;
		}

		public static boolean addWarning(WsIOElement element) {
			if (State.warnings.get() == null) {
				State.warnings.set(new ArrayList<>());
			}
			return State.warnings.get().add(element);
		}

		public static boolean removeWarning(WsIOElement element) {
			if (State.warnings.get() != null) {
				return State.warnings.get().remove(element);
			}
			return false;
		}

		public static boolean addSuccessfulOf(String identifier,
		                                      WsIOText message,
		                                      WsIOText description,
		                                      String type) {
			return addSuccessful(WsIOElement.of(identifier, message, description, type));
		}

		public static boolean removeSuccessfulOf(String identifier,
		                                         WsIOText message,
		                                         WsIOText description,
		                                         String type) {
			return removeSuccessful(WsIOElement.of(identifier, message, description, type));
		}

		public static boolean addFailureOf(String identifier,
		                                   WsIOText message,
		                                   WsIOText description,
		                                   String type) {
			return addFailure(WsIOElement.of(identifier, message, description, type));
		}

		public static boolean removeFailureOf(String identifier,
		                                      WsIOText message,
		                                      WsIOText description,
		                                      String type) {
			return removeFailure(WsIOElement.of(identifier, message, description, type));
		}

		public static boolean addWarningOf(String identifier,
		                                   WsIOText message,
		                                   WsIOText description,
		                                   String type) {
			return addWarning(WsIOElement.of(identifier, message, description, type));
		}

		public static boolean removeWarningOf(String identifier,
		                                      WsIOText message,
		                                      WsIOText description,
		                                      String type) {
			return removeWarning(WsIOElement.of(identifier, message, description, type));
		}

		public static boolean addSuccessfulId(String identifier) {
			return addSuccessful(WsIOElement.id(identifier));
		}

		public static boolean removeSuccessfulId(String identifier) {
			return removeSuccessful(WsIOElement.id(identifier));
		}

		public static boolean addFailureId(String identifier) {
			return addFailure(WsIOElement.id(identifier));
		}

		public static boolean removeFailureId(String identifier) {
			return removeFailure(WsIOElement.id(identifier));
		}

		public static boolean addWarningId(String identifier) {
			return addWarning(WsIOElement.id(identifier));
		}

		public static boolean removeWarningId(String identifier) {
			return removeWarning(WsIOElement.id(identifier));
		}

		public static boolean addSuccessfulMessage(WsIOText message) {
			return addSuccessful(WsIOElement.message(message));
		}

		public static boolean removeSuccessfulMessage(WsIOText message) {
			return removeSuccessful(WsIOElement.message(message));
		}

		public static boolean addFailureMessage(WsIOText message) {
			return addFailure(WsIOElement.message(message));
		}

		public static boolean removeFailureMessage(WsIOText message) {
			return removeFailure(WsIOElement.message(message));
		}

		public static boolean addWarningMessage(WsIOText message) {
			return addWarning(WsIOElement.message(message));
		}

		public static boolean removeWarningMessage(WsIOText message) {
			return removeWarning(WsIOElement.message(message));
		}

		public static boolean addSuccessfulDescription(WsIOText description) {
			return addSuccessful(WsIOElement.description(description));
		}

		public static boolean removeSuccessfulDescription(WsIOText description) {
			return removeSuccessful(WsIOElement.description(description));
		}

		public static boolean addFailureDescription(WsIOText description) {
			return addFailure(WsIOElement.description(description));
		}

		public static boolean removeFailureDescription(WsIOText description) {
			return removeFailure(WsIOElement.description(description));
		}

		public static boolean addWarningDescription(WsIOText description) {
			return addWarning(WsIOElement.description(description));
		}

		public static boolean removeWarningDescription(WsIOText description) {
			return removeWarning(WsIOElement.description(description));
		}

		public static boolean addSuccessfulType(String type) {
			return addSuccessful(WsIOElement.type(type));
		}

		public static boolean removeSuccessfulType(String type) {
			return removeSuccessful(WsIOElement.type(type));
		}

		public static boolean addFailureType(String type) {
			return addFailure(WsIOElement.type(type));
		}

		public static boolean removeFailureType(String type) {
			return removeFailure(WsIOElement.type(type));
		}

		public static boolean addWarningType(String type) {
			return addWarning(WsIOElement.type(type));
		}

		public static boolean removeWarningType(String type) {
			return removeWarning(WsIOElement.type(type));
		}

		public static boolean addSuccessfulMessageOf(WsIOLanguage language, String message) {
			return addSuccessful(WsIOElement.messageOf(language, message));
		}

		public static boolean removeSuccessfulMessageOf(WsIOLanguage language, String message) {
			return removeSuccessful(WsIOElement.messageOf(language, message));
		}

		public static boolean addFailureMessageOf(WsIOLanguage language, String message) {
			return addFailure(WsIOElement.messageOf(language, message));
		}

		public static boolean removeFailureMessageOf(WsIOLanguage language, String message) {
			return removeFailure(WsIOElement.messageOf(language, message));
		}

		public static boolean addWarningMessageOf(WsIOLanguage language, String message) {
			return addWarning(WsIOElement.messageOf(language, message));
		}

		public static boolean removeWarningMessageOf(WsIOLanguage language, String message) {
			return removeWarning(WsIOElement.messageOf(language, message));
		}

		public static boolean addSuccessfulDescriptionOf(WsIOLanguage language, String description) {
			return addSuccessful(WsIOElement.descriptionOf(language, description));
		}

		public static boolean removeSuccessfulDescriptionOf(WsIOLanguage language, String description) {
			return removeSuccessful(WsIOElement.descriptionOf(language, description));
		}

		public static boolean addFailureDescriptionOf(WsIOLanguage language, String description) {
			return addFailure(WsIOElement.descriptionOf(language, description));
		}

		public static boolean removeFailureDescriptionOf(WsIOLanguage language, String description) {
			return removeFailure(WsIOElement.descriptionOf(language, description));
		}

		public static boolean addWarningDescriptionOf(WsIOLanguage language, String description) {
			return addWarning(WsIOElement.descriptionOf(language, description));
		}

		public static boolean removeWarningDescriptionOf(WsIOLanguage language, String description) {
			return removeWarning(WsIOElement.descriptionOf(language, description));
		}

		public static boolean addSuccessfulMessageDef(String message) {
			return addSuccessful(WsIOElement.messageDef(message));
		}

		public static boolean removeSuccessfulMessageDef(String message) {
			return removeSuccessful(WsIOElement.messageDef(message));
		}

		public static boolean addFailureMessageDef(String message) {
			return addFailure(WsIOElement.messageDef(message));
		}

		public static boolean removeFailureMessageDef(String message) {
			return removeFailure(WsIOElement.messageDef(message));
		}

		public static boolean addWarningMessageDef(String message) {
			return addWarning(WsIOElement.messageDef(message));
		}

		public static boolean removeWarningMessageDef(String message) {
			return removeWarning(WsIOElement.messageDef(message));
		}

		public static boolean addSuccessfulDescriptionDef(String description) {
			return addSuccessful(WsIOElement.descriptionDef(description));
		}

		public static boolean removeSuccessfulDescriptionDef(String description) {
			return removeSuccessful(WsIOElement.descriptionDef(description));
		}

		public static boolean addFailureDescriptionDef(String description) {
			return addFailure(WsIOElement.descriptionDef(description));
		}

		public static boolean removeFailureDescriptionDef(String description) {
			return removeFailure(WsIOElement.descriptionDef(description));
		}

		public static boolean addWarningDescriptionDef(String description) {
			return addWarning(WsIOElement.descriptionDef(description));
		}

		public static boolean removeWarningDescriptionDef(String description) {
			return removeWarning(WsIOElement.descriptionDef(description));
		}

		public static boolean addSuccessfulMessageNolang(String message) {
			return addSuccessful(WsIOElement.messageNoLang(message));
		}

		public static boolean removeSuccessfulMessageNolang(String message) {
			return removeSuccessful(WsIOElement.messageNoLang(message));
		}

		public static boolean addFailureMessageNolang(String message) {
			return addFailure(WsIOElement.messageNoLang(message));
		}

		public static boolean removeFailureMessageNolang(String message) {
			return removeFailure(WsIOElement.messageNoLang(message));
		}

		public static boolean addWarningMessageNolang(String message) {
			return addWarning(WsIOElement.messageNoLang(message));
		}

		public static boolean removeWarningMessageNolang(String message) {
			return removeWarning(WsIOElement.messageNoLang(message));
		}

		public static boolean addSuccessfulDescriptionNolang(String description) {
			return addSuccessful(WsIOElement.descriptionNoLang(description));
		}

		public static boolean removeSuccessfulDescriptionNolang(String description) {
			return removeSuccessful(WsIOElement.descriptionNoLang(description));
		}

		public static boolean addFailureDescriptionNolang(String description) {
			return addFailure(WsIOElement.descriptionNoLang(description));
		}

		public static boolean removeFailureDescriptionNolang(String description) {
			return removeFailure(WsIOElement.descriptionNoLang(description));
		}

		public static boolean addWarningDescriptionNolang(String description) {
			return addWarning(WsIOElement.descriptionNoLang(description));
		}

		public static boolean removeWarningDescriptionNolang(String description) {
			return removeWarning(WsIOElement.descriptionNoLang(description));
		}

		public static boolean addSuccessfulBasic(String identifier,
		                                         WsIOText message) {
			return addSuccessful(WsIOElement.basic(identifier, message));
		}

		public static boolean removeSuccessfulBasic(String identifier,
		                                            WsIOText message) {
			return removeSuccessful(WsIOElement.basic(identifier, message));
		}

		public static boolean addFailureBasic(String identifier,
		                                      WsIOText message) {
			return addFailure(WsIOElement.basic(identifier, message));
		}

		public static boolean removeFailureBasic(String identifier,
		                                         WsIOText message) {
			return removeFailure(WsIOElement.basic(identifier, message));
		}

		public static boolean addWarningBasic(String identifier,
		                                      WsIOText message) {
			return addWarning(WsIOElement.basic(identifier, message));
		}

		public static boolean removeWarningBasic(String identifier,
		                                         WsIOText message) {
			return removeWarning(WsIOElement.basic(identifier, message));
		}

		public static boolean addSuccessfulSimple(WsIOText message,
		                                          String type) {
			return addSuccessful(WsIOElement.simple(message, type));
		}

		public static boolean removeSuccessfulSimple(WsIOText message,
		                                             String type) {
			return removeSuccessful(WsIOElement.simple(message, type));
		}

		public static boolean addFailureSimple(WsIOText message,
		                                       String type) {
			return addFailure(WsIOElement.simple(message, type));
		}

		public static boolean removeFailureSimple(WsIOText message,
		                                          String type) {
			return removeFailure(WsIOElement.simple(message, type));
		}

		public static boolean addWarningSimple(WsIOText message,
		                                       String type) {
			return addWarning(WsIOElement.simple(message, type));
		}

		public static boolean removeWarningSimple(WsIOText message,
		                                          String type) {
			return removeWarning(WsIOElement.simple(message, type));
		}

		public static boolean addSuccessfulText(WsIOText message,
		                                        WsIOText description) {
			return addSuccessful(WsIOElement.text(message, description));
		}

		public static boolean removeSuccessfulText(WsIOText message,
		                                           WsIOText description) {
			return removeSuccessful(WsIOElement.text(message, description));
		}

		public static boolean addFailureText(WsIOText message,
		                                     WsIOText description) {
			return addFailure(WsIOElement.text(message, description));
		}

		public static boolean removeFailureText(WsIOText message,
		                                        WsIOText description) {
			return removeFailure(WsIOElement.text(message, description));
		}

		public static boolean addWarningText(WsIOText message,
		                                     WsIOText description) {
			return addWarning(WsIOElement.text(message, description));
		}

		public static boolean removeWarningText(WsIOText message,
		                                        WsIOText description) {
			return removeWarning(WsIOElement.text(message, description));
		}

		public static boolean addSuccessfulTyped(String identifier,
		                                         String type) {
			return addSuccessful(WsIOElement.typed(identifier, type));
		}

		public static boolean removeSuccessfulTyped(String identifier,
		                                            String type) {
			return removeSuccessful(WsIOElement.typed(identifier, type));
		}

		public static boolean addFailureTyped(String identifier,
		                                      String type) {
			return addFailure(WsIOElement.typed(identifier, type));
		}

		public static boolean removeFailureTyped(String identifier,
		                                         String type) {
			return removeFailure(WsIOElement.typed(identifier, type));
		}

		public static boolean addWarningTyped(String identifier,
		                                      String type) {
			return addWarning(WsIOElement.typed(identifier, type));
		}

		public static boolean removeWarningTyped(String identifier,
		                                         String type) {
			return removeWarning(WsIOElement.typed(identifier, type));
		}

		public static boolean addSuccessfulNotype(String identifier,
		                                          WsIOText message,
		                                          WsIOText description) {
			return addSuccessful(WsIOElement.noType(identifier, message, description));
		}

		public static boolean removeSuccessfulNotype(String identifier,
		                                             WsIOText message,
		                                             WsIOText description) {
			return removeSuccessful(WsIOElement.noType(identifier, message, description));
		}

		public static boolean addFailureNotype(String identifier,
		                                       WsIOText message,
		                                       WsIOText description) {
			return addFailure(WsIOElement.noType(identifier, message, description));
		}

		public static boolean removeFailureNotype(String identifier,
		                                          WsIOText message,
		                                          WsIOText description) {
			return removeFailure(WsIOElement.noType(identifier, message, description));
		}

		public static boolean addWarningNotype(String identifier,
		                                       WsIOText message,
		                                       WsIOText description) {
			return addWarning(WsIOElement.noType(identifier, message, description));
		}

		public static boolean removeWarningNotype(String identifier,
		                                          WsIOText message,
		                                          WsIOText description) {
			return removeWarning(WsIOElement.noType(identifier, message, description));
		}

		public static boolean addSuccessfulNodesc(String identifier,
		                                          WsIOText message,
		                                          String type) {
			return addSuccessful(WsIOElement.noDesc(identifier, message, type));
		}

		public static boolean removeSuccessfulNodesc(String identifier,
		                                             WsIOText message,
		                                             String type) {
			return removeSuccessful(WsIOElement.noDesc(identifier, message, type));
		}

		public static boolean addFailureNodesc(String identifier,
		                                       WsIOText message,
		                                       String type) {
			return addFailure(WsIOElement.noDesc(identifier, message, type));
		}

		public static boolean removeFailureNodesc(String identifier,
		                                          WsIOText message,
		                                          String type) {
			return removeFailure(WsIOElement.noDesc(identifier, message, type));
		}

		public static boolean addWarningNodesc(String identifier,
		                                       WsIOText message,
		                                       String type) {
			return addWarning(WsIOElement.noDesc(identifier, message, type));
		}

		public static boolean removeWarningNodesc(String identifier,
		                                          WsIOText message,
		                                          String type) {
			return removeWarning(WsIOElement.noDesc(identifier, message, type));
		}

		public static boolean addSuccessfulNoid(WsIOText message,
		                                        WsIOText description,
		                                        String type) {
			return addSuccessful(WsIOElement.noId(message, description, type));
		}

		public static boolean removeSuccessfulNoid(WsIOText message,
		                                           WsIOText description,
		                                           String type) {
			return removeSuccessful(WsIOElement.noId(message, description, type));
		}

		public static boolean addFailureNoid(WsIOText message,
		                                     WsIOText description,
		                                     String type) {
			return addFailure(WsIOElement.noId(message, description, type));
		}

		public static boolean removeFailureNoid(WsIOText message,
		                                        WsIOText description,
		                                        String type) {
			return removeFailure(WsIOElement.noId(message, description, type));
		}

		public static boolean addWarningNoid(WsIOText message,
		                                     WsIOText description,
		                                     String type) {
			return addWarning(WsIOElement.noId(message, description, type));
		}

		public static boolean removeWarningNoid(WsIOText message,
		                                        WsIOText description,
		                                        String type) {
			return removeWarning(WsIOElement.noId(message, description, type));
		}

		public static boolean addSuccessfulBasicOf(String identifier,
		                                           WsIOLanguage language,
		                                           String message) {
			return addSuccessful(WsIOElement.basicOf(identifier, language, message));
		}

		public static boolean removeSuccessfulBasicOf(String identifier,
		                                              WsIOLanguage language,
		                                              String message) {
			return removeSuccessful(WsIOElement.basicOf(identifier, language, message));
		}

		public static boolean addFailureBasicOf(String identifier,
		                                        WsIOLanguage language,
		                                        String message) {
			return addFailure(WsIOElement.basicOf(identifier, language, message));
		}

		public static boolean removeFailureBasicOf(String identifier,
		                                           WsIOLanguage language,
		                                           String message) {
			return removeFailure(WsIOElement.basicOf(identifier, language, message));
		}

		public static boolean addWarningBasicOf(String identifier,
		                                        WsIOLanguage language,
		                                        String message) {
			return addWarning(WsIOElement.basicOf(identifier, language, message));
		}

		public static boolean removeWarningBasicOf(String identifier,
		                                           WsIOLanguage language,
		                                           String message) {
			return removeWarning(WsIOElement.basicOf(identifier, language, message));
		}

		public static boolean addSuccessfulSimpleOf(WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return addSuccessful(WsIOElement.simpleOf(language, message, type));
		}

		public static boolean removeSuccessfulSimpleOf(WsIOLanguage language,
		                                               String message,
		                                               String type) {
			return removeSuccessful(WsIOElement.simpleOf(language, message, type));
		}

		public static boolean addFailureSimpleOf(WsIOLanguage language,
		                                         String message,
		                                         String type) {
			return addFailure(WsIOElement.simpleOf(language, message, type));
		}

		public static boolean removeFailureSimpleOf(WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return removeFailure(WsIOElement.simpleOf(language, message, type));
		}

		public static boolean addWarningSimpleOf(WsIOLanguage language,
		                                         String message,
		                                         String type) {
			return addWarning(WsIOElement.simpleOf(language, message, type));
		}

		public static boolean removeWarningSimpleOf(WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return removeWarning(WsIOElement.simpleOf(language, message, type));
		}

		public static boolean addSuccessfulTextOf(WsIOLanguage language,
		                                          String message,
		                                          String description) {
			return addSuccessful(WsIOElement.textOf(language, message, description));
		}

		public static boolean removeSuccessfulTextOf(WsIOLanguage language,
		                                             String message,
		                                             String description) {
			return removeSuccessful(WsIOElement.textOf(language, message, description));
		}

		public static boolean addFailureTextOf(WsIOLanguage language,
		                                       String message,
		                                       String description) {
			return addFailure(WsIOElement.textOf(language, message, description));
		}

		public static boolean removeFailureTextOf(WsIOLanguage language,
		                                          String message,
		                                          String description) {
			return removeFailure(WsIOElement.textOf(language, message, description));
		}

		public static boolean addWarningTextOf(WsIOLanguage language,
		                                       String message,
		                                       String description) {
			return addWarning(WsIOElement.textOf(language, message, description));
		}

		public static boolean removeWarningTextOf(WsIOLanguage language,
		                                          String message,
		                                          String description) {
			return removeWarning(WsIOElement.textOf(language, message, description));
		}

		public static boolean addSuccessfulBasicDef(String identifier,
		                                            String message) {
			return addSuccessful(WsIOElement.basicDef(identifier, message));
		}

		public static boolean removeSuccessfulBasicDef(String identifier,
		                                               String message) {
			return removeSuccessful(WsIOElement.basicDef(identifier, message));
		}

		public static boolean addFailureBasicDef(String identifier,
		                                         String message) {
			return addFailure(WsIOElement.basicDef(identifier, message));
		}

		public static boolean removeFailureBasicDef(String identifier,
		                                            String message) {
			return removeFailure(WsIOElement.basicDef(identifier, message));
		}

		public static boolean addWarningBasicDef(String identifier,
		                                         String message) {
			return addWarning(WsIOElement.basicDef(identifier, message));
		}

		public static boolean removeWarningBasicDef(String identifier,
		                                            String message) {
			return removeWarning(WsIOElement.basicDef(identifier, message));
		}

		public static boolean addSuccessfulSimpleDef(String message,
		                                             String type) {
			return addSuccessful(WsIOElement.simpleDef(message, type));
		}

		public static boolean removeSuccessfulSimpleDef(String message,
		                                                String type) {
			return removeSuccessful(WsIOElement.simpleDef(message, type));
		}

		public static boolean addFailureSimpleDef(String message,
		                                          String type) {
			return addFailure(WsIOElement.simpleDef(message, type));
		}

		public static boolean removeFailureSimpleDef(String message,
		                                             String type) {
			return removeFailure(WsIOElement.simpleDef(message, type));
		}

		public static boolean addWarningSimpleDef(String message,
		                                          String type) {
			return addWarning(WsIOElement.simpleDef(message, type));
		}

		public static boolean removeWarningSimpleDef(String message,
		                                             String type) {
			return removeWarning(WsIOElement.simpleDef(message, type));
		}

		public static boolean addSuccessfulTextDef(String message,
		                                           String description) {
			return addSuccessful(WsIOElement.textDef(message, description));
		}

		public static boolean removeSuccessfulTextDef(String message,
		                                              String description) {
			return removeSuccessful(WsIOElement.textDef(message, description));
		}

		public static boolean addFailureTextDef(String message,
		                                        String description) {
			return addFailure(WsIOElement.textDef(message, description));
		}

		public static boolean removeFailureTextDef(String message,
		                                           String description) {
			return removeFailure(WsIOElement.textDef(message, description));
		}

		public static boolean addWarningTextDef(String message,
		                                        String description) {
			return addWarning(WsIOElement.textDef(message, description));
		}

		public static boolean removeWarningTextDef(String message,
		                                           String description) {
			return removeWarning(WsIOElement.textDef(message, description));
		}

		public static boolean addSuccessfulBasicNolang(String identifier,
		                                               String message) {
			return addSuccessful(WsIOElement.basicNoLang(identifier, message));
		}

		public static boolean removeSuccessfulBasicNolang(String identifier,
		                                                  String message) {
			return removeSuccessful(WsIOElement.basicNoLang(identifier, message));
		}

		public static boolean addFailureBasicNolang(String identifier,
		                                            String message) {
			return addFailure(WsIOElement.basicNoLang(identifier, message));
		}

		public static boolean removeFailureBasicNolang(String identifier,
		                                               String message) {
			return removeFailure(WsIOElement.basicNoLang(identifier, message));
		}

		public static boolean addWarningBasicNolang(String identifier,
		                                            String message) {
			return addWarning(WsIOElement.basicNoLang(identifier, message));
		}

		public static boolean removeWarningBasicNolang(String identifier,
		                                               String message) {
			return removeWarning(WsIOElement.basicNoLang(identifier, message));
		}

		public static boolean addSuccessfulSimpleNolang(String message,
		                                                String type) {
			return addSuccessful(WsIOElement.simpleNoLang(message, type));
		}

		public static boolean removeSuccessfulSimpleNolang(String message,
		                                                   String type) {
			return removeSuccessful(WsIOElement.simpleNoLang(message, type));
		}

		public static boolean addFailureSimpleNolang(String message,
		                                             String type) {
			return addFailure(WsIOElement.simpleNoLang(message, type));
		}

		public static boolean removeFailureSimpleNolang(String message,
		                                                String type) {
			return removeFailure(WsIOElement.simpleNoLang(message, type));
		}

		public static boolean addWarningSimpleNolang(String message,
		                                             String type) {
			return addWarning(WsIOElement.simpleNoLang(message, type));
		}

		public static boolean removeWarningSimpleNolang(String message,
		                                                String type) {
			return removeWarning(WsIOElement.simpleNoLang(message, type));
		}

		public static boolean addSuccessfulTextNolang(String message,
		                                              String description) {
			return addSuccessful(WsIOElement.textNoLang(message, description));
		}

		public static boolean removeSuccessfulTextNolang(String message,
		                                                 String description) {
			return removeSuccessful(WsIOElement.textNoLang(message, description));
		}

		public static boolean addFailureTextNolang(String message,
		                                           String description) {
			return addFailure(WsIOElement.textNoLang(message, description));
		}

		public static boolean removeFailureTextNolang(String message,
		                                              String description) {
			return removeFailure(WsIOElement.textNoLang(message, description));
		}

		public static boolean addWarningTextNolang(String message,
		                                           String description) {
			return addWarning(WsIOElement.textNoLang(message, description));
		}

		public static boolean removeWarningTextNolang(String message,
		                                              String description) {
			return removeWarning(WsIOElement.textNoLang(message, description));
		}

		public static boolean addSuccessfulNotypeOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String description) {
			return addSuccessful(WsIOElement.noTypeOf(identifier, language, message, description));
		}

		public static boolean removeSuccessfulNotypeOf(String identifier,
		                                               WsIOLanguage language,
		                                               String message,
		                                               String description) {
			return removeSuccessful(WsIOElement.noTypeOf(identifier, language, message, description));
		}

		public static boolean addFailureNotypeOf(String identifier,
		                                         WsIOLanguage language,
		                                         String message,
		                                         String description) {
			return addFailure(WsIOElement.noTypeOf(identifier, language, message, description));
		}

		public static boolean removeFailureNotypeOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String description) {
			return removeFailure(WsIOElement.noTypeOf(identifier, language, message, description));
		}

		public static boolean addWarningNotypeOf(String identifier,
		                                         WsIOLanguage language,
		                                         String message,
		                                         String description) {
			return addWarning(WsIOElement.noTypeOf(identifier, language, message, description));
		}

		public static boolean removeWarningNotypeOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String description) {
			return removeWarning(WsIOElement.noTypeOf(identifier, language, message, description));
		}

		public static boolean addSuccessfulNodescOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return addSuccessful(WsIOElement.noDescOf(identifier, language, message, type));
		}

		public static boolean removeSuccessfulNodescOf(String identifier,
		                                               WsIOLanguage language,
		                                               String message,
		                                               String type) {
			return removeSuccessful(WsIOElement.noDescOf(identifier, language, message, type));
		}

		public static boolean addFailureNodescOf(String identifier,
		                                         WsIOLanguage language,
		                                         String message,
		                                         String type) {
			return addFailure(WsIOElement.noDescOf(identifier, language, message, type));
		}

		public static boolean removeFailureNodescOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return removeFailure(WsIOElement.noDescOf(identifier, language, message, type));
		}

		public static boolean addWarningNodescOf(String identifier,
		                                         WsIOLanguage language,
		                                         String message,
		                                         String type) {
			return addWarning(WsIOElement.noDescOf(identifier, language, message, type));
		}

		public static boolean removeWarningNodescOf(String identifier,
		                                            WsIOLanguage language,
		                                            String message,
		                                            String type) {
			return removeWarning(WsIOElement.noDescOf(identifier, language, message, type));
		}

		public static boolean addSuccessfulNoidOf(WsIOLanguage language,
		                                          String message,
		                                          String description,
		                                          String type) {
			return addSuccessful(WsIOElement.noIdOf(language, message, description, type));
		}

		public static boolean removeSuccessfulNoidOf(WsIOLanguage language,
		                                             String message,
		                                             String description,
		                                             String type) {
			return removeSuccessful(WsIOElement.noIdOf(language, message, description, type));
		}

		public static boolean addFailureNoidOf(WsIOLanguage language,
		                                       String message,
		                                       String description,
		                                       String type) {
			return addFailure(WsIOElement.noIdOf(language, message, description, type));
		}

		public static boolean removeFailureNoidOf(WsIOLanguage language,
		                                          String message,
		                                          String description,
		                                          String type) {
			return removeFailure(WsIOElement.noIdOf(language, message, description, type));
		}

		public static boolean addWarningNoidOf(WsIOLanguage language,
		                                       String message,
		                                       String description,
		                                       String type) {
			return addWarning(WsIOElement.noIdOf(language, message, description, type));
		}

		public static boolean removeWarningNoidOf(WsIOLanguage language,
		                                          String message,
		                                          String description,
		                                          String type) {
			return removeWarning(WsIOElement.noIdOf(language, message, description, type));
		}

		public static boolean addSuccessfulNotypeDef(String identifier,
		                                             String message,
		                                             String description) {
			return addSuccessful(WsIOElement.noTypeDef(identifier, message, description));
		}

		public static boolean removeSuccessfulNotypeDef(String identifier,
		                                                String message,
		                                                String description) {
			return removeSuccessful(WsIOElement.noTypeDef(identifier, message, description));
		}

		public static boolean addFailureNotypeDef(String identifier,
		                                          String message,
		                                          String description) {
			return addFailure(WsIOElement.noTypeDef(identifier, message, description));
		}

		public static boolean removeFailureNotypeDef(String identifier,
		                                             String message,
		                                             String description) {
			return removeFailure(WsIOElement.noTypeDef(identifier, message, description));
		}

		public static boolean addWarningNotypeDef(String identifier,
		                                          String message,
		                                          String description) {
			return addWarning(WsIOElement.noTypeDef(identifier, message, description));
		}

		public static boolean removeWarningNotypeDef(String identifier,
		                                             String message,
		                                             String description) {
			return removeWarning(WsIOElement.noTypeDef(identifier, message, description));
		}

		public static boolean addSuccessfulNodescDef(String identifier,
		                                             String message,
		                                             String type) {
			return addSuccessful(WsIOElement.noDescDef(identifier, message, type));
		}

		public static boolean removeSuccessfulNodescDef(String identifier,
		                                                String message,
		                                                String type) {
			return removeSuccessful(WsIOElement.noDescDef(identifier, message, type));
		}

		public static boolean addFailureNodescDef(String identifier,
		                                          String message,
		                                          String type) {
			return addFailure(WsIOElement.noDescDef(identifier, message, type));
		}

		public static boolean removeFailureNodescDef(String identifier,
		                                             String message,
		                                             String type) {
			return removeFailure(WsIOElement.noDescDef(identifier, message, type));
		}

		public static boolean addWarningNodescDef(String identifier,
		                                          String message,
		                                          String type) {
			return addWarning(WsIOElement.noDescDef(identifier, message, type));
		}

		public static boolean removeWarningNodescDef(String identifier,
		                                             String message,
		                                             String type) {
			return removeWarning(WsIOElement.noDescDef(identifier, message, type));
		}

		public static boolean addSuccessfulNoidDef(String message,
		                                           String description,
		                                           String type) {
			return addSuccessful(WsIOElement.noIdDef(message, description, type));
		}

		public static boolean removeSuccessfulNoidDef(String message,
		                                              String description,
		                                              String type) {
			return removeSuccessful(WsIOElement.noIdDef(message, description, type));
		}

		public static boolean addFailureNoidDef(String message,
		                                        String description,
		                                        String type) {
			return addFailure(WsIOElement.noIdDef(message, description, type));
		}

		public static boolean removeFailureNoidDef(String message,
		                                           String description,
		                                           String type) {
			return removeFailure(WsIOElement.noIdDef(message, description, type));
		}

		public static boolean addWarningNoidDef(String message,
		                                        String description,
		                                        String type) {
			return addWarning(WsIOElement.noIdDef(message, description, type));
		}

		public static boolean removeWarningNoidDef(String message,
		                                           String description,
		                                           String type) {
			return removeWarning(WsIOElement.noIdDef(message, description, type));
		}

		public static boolean addSuccessfulNotypeNolang(String identifier,
		                                                String message,
		                                                String description) {
			return addSuccessful(WsIOElement.noTypeNoLang(identifier, message, description));
		}

		public static boolean removeSuccessfulNotypeNolang(String identifier,
		                                                   String message,
		                                                   String description) {
			return removeSuccessful(WsIOElement.noTypeNoLang(identifier, message, description));
		}

		public static boolean addFailureNotypeNolang(String identifier,
		                                             String message,
		                                             String description) {
			return addFailure(WsIOElement.noTypeNoLang(identifier, message, description));
		}

		public static boolean removeFailureNotypeNolang(String identifier,
		                                                String message,
		                                                String description) {
			return removeFailure(WsIOElement.noTypeNoLang(identifier, message, description));
		}

		public static boolean addWarningNotypeNolang(String identifier,
		                                             String message,
		                                             String description) {
			return addWarning(WsIOElement.noTypeNoLang(identifier, message, description));
		}

		public static boolean removeWarningNotypeNolang(String identifier,
		                                                String message,
		                                                String description) {
			return removeWarning(WsIOElement.noTypeNoLang(identifier, message, description));
		}

		public static boolean addSuccessfulNodescNolang(String identifier,
		                                                String message,
		                                                String type) {
			return addSuccessful(WsIOElement.noDescNoLang(identifier, message, type));
		}

		public static boolean removeSuccessfulNodescNolang(String identifier,
		                                                   String message,
		                                                   String type) {
			return removeSuccessful(WsIOElement.noDescNoLang(identifier, message, type));
		}

		public static boolean addFailureNodescNolang(String identifier,
		                                             String message,
		                                             String type) {
			return addFailure(WsIOElement.noDescNoLang(identifier, message, type));
		}

		public static boolean removeFailureNodescNolang(String identifier,
		                                                String message,
		                                                String type) {
			return removeFailure(WsIOElement.noDescNoLang(identifier, message, type));
		}

		public static boolean addWarningNodescNolang(String identifier,
		                                             String message,
		                                             String type) {
			return addWarning(WsIOElement.noDescNoLang(identifier, message, type));
		}

		public static boolean removeWarningNodescNolang(String identifier,
		                                                String message,
		                                                String type) {
			return removeWarning(WsIOElement.noDescNoLang(identifier, message, type));
		}

		public static boolean addSuccessfulNoidNolang(String message,
		                                              String description,
		                                              String type) {
			return addSuccessful(WsIOElement.noIdNoLang(message, description, type));
		}

		public static boolean removeSuccessfulNoidNolang(String message,
		                                                 String description,
		                                                 String type) {
			return removeSuccessful(WsIOElement.noIdNoLang(message, description, type));
		}

		public static boolean addFailureNoidNolang(String message,
		                                           String description,
		                                           String type) {
			return addFailure(WsIOElement.noIdNoLang(message, description, type));
		}

		public static boolean removeFailureNoidNolang(String message,
		                                              String description,
		                                              String type) {
			return removeFailure(WsIOElement.noIdNoLang(message, description, type));
		}

		public static boolean addWarningNoidNolang(String message,
		                                           String description,
		                                           String type) {
			return addWarning(WsIOElement.noIdNoLang(message, description, type));
		}

		public static boolean removeWarningNoidNolang(String message,
		                                              String description,
		                                              String type) {
			return removeWarning(WsIOElement.noIdNoLang(message, description, type));
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