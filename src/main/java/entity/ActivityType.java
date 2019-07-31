package entity;

import java.time.Duration;

public enum ActivityType {
	//          Timeout               Min. displayed Duration  svgY svgHeight svgColour
	ACTIVE      (Duration.ofSeconds(60), Duration.ofSeconds(5)),
	WRITE       (Duration.ofSeconds(60), Duration.ofSeconds(5)),
	DEBUG       (Duration.ofSeconds(60), Duration.ofSeconds(5)),
	TESTINGSTATE(Duration.ofSeconds(0),  Duration.ofSeconds(0));
	
	private final Duration timeoutDuration;
	private final Duration minDisplayedDuration;
	
	private ActivityType(Duration timeoutDuration, Duration minDisplayDuration) {
		this.timeoutDuration      = timeoutDuration;
		this.minDisplayedDuration = minDisplayDuration;
	}

	public Duration timeoutDuration() {
		return timeoutDuration;
	}

	public Duration minDisplayedDuration() {
		return minDisplayedDuration;
	}
}
