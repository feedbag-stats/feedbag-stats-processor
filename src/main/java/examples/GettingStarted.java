/**
 * Copyright 2016 University of Zurich
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package examples;

import java.io.File;
import java.util.Set;

import aggregation.SessionRecord;
import cc.kave.commons.model.events.IDEEvent;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.ReadingArchive;

/**
 * Simple example that shows how the interaction dataset can be opened, all
 * users identified, and all contained events deserialized.
 */
public class GettingStarted {

	private String eventsDir;

	public GettingStarted(String eventsDir) {
		this.eventsDir = eventsDir;
	}

	public void run() {

		System.out.printf("looking (recursively) for events in folder %s\n", new File(eventsDir).getAbsolutePath());

		/*
		 * Each .zip that is contained in the eventsDir represents all events that we
		 * have collected for a specific user, the folder represents the first day when
		 * the user uploaded data.
		 */
		Set<String> userZips = IoHelper.findAllZips(eventsDir);
		System.out.printf("found %d zips\n", userZips.size());

		int numZip = 0;
		for (String userZip : userZips) {
			numZip++;
			System.out.printf("\n#### processing user zip %d/%d: %s #####\n", numZip, userZips.size(), userZip);
			processUserZip(userZip);
		}
	}

	private void processUserZip(String userZip) {
		int numProcessedEvents = 0;
		// open the .zip file ...
		try (IReadingArchive ra = new ReadingArchive(new File(eventsDir, userZip))) {
			// ... and iterate over content.
			// the iteration will stop after 200 events to speed things up, remove this
			// guard to process all events.
			SessionRecord record = new SessionRecord();
			while (ra.hasNext() /*&& (numProcessedEvents++ < 20)*/) {
				/*
				 * within the userZip, each stored event is contained as a single file that
				 * contains the Json representation of a subclass of IDEEvent.
				 */
				IDEEvent e = ra.getNext(IDEEvent.class);

				record.setSessId(e.IDESessionUUID);
				record.addEvent(e);
				//processEvent(e);
				numProcessedEvents++;
				if(numProcessedEvents%10000 == 0) System.out.println(numProcessedEvents);
			}
			
			System.out.printf("%s contains %d events\n", userZip, numProcessedEvents);
			System.out.print(record.toString());
			System.out.println(record.toSVG());
		}
	}
}