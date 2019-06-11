
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

import static cc.kave.commons.utils.io.Logger.log;

import cc.kave.commons.utils.io.Logger;
import cc.kave.rsse.calls.mining.Options;
import cc.kave.rsse.calls.utils.OptionsBuilder;
import cc.kave.rsse.calls.utils.json.JsonUtilsCcKaveRsseCalls;
import entity.ActivityEntry;
import entity.User;
import examples.GettingStarted;
import examples.rsse.calls.BMNEvaluation;
import examples.rsse.calls.BMNMining;
import helpers.HibernateUtil;
import org.hibernate.Session;

import javax.persistence.Query;
import java.util.List;
import java.util.UUID;

public class RunMe {

	// private static final String dirRoot = "/path/to/folder/";
	private static final String dirRoot = "/home/kitty/Desktop/uni/mp/java-cc-kave-examples-master/";

	/*
	 * download the interaction data and unzip it into the root of this project (at
	 * the level of the pom.xml). Unpack it, you should now have a folder that
	 * includes a bunch of folders that have dates as names and that contain .zip
	 * files.
	 */
	public static String dirEvents = dirRoot + "Events-170301-2/";

	/*
	 * download the context data and follow the same instructions as before.
	 */
	public static String dirContexts = dirRoot + "Contexts-170503/";

	public static void main(String[] args) {
		init();

		// examples for BASIC DATA READING

		new GettingStarted(dirEvents).run();

		// RSSE related examples

		//runRoundtrip_BMN();

		//hibernateExample();
		System.out.println("Done");
	}

	private static void runRoundtrip_BMN() {
		Options opts = OptionsBuilder.bmn().cCtx(true).mCtx(true).def(true).calls(true).params(true).members(true)
				.atLeast(5).get();

		String dirSortedUsages = dirRoot + "usages/";
		String dirBmnModels = dirRoot + "models/bmn/";

		// Invoke this mining step to build models from scratch. Once this has been
		// completed, you will find all models in the "dirBmnModels" folder and you can
		// comment it out to significantly speed-up future executions.
		new BMNMining(opts, dirContexts, dirSortedUsages, dirBmnModels).run();

		// The evaluation assumes that you have already mined (or downloaded) BMN models
		// and that they are contained in the "dirBmnModels" folder.
		new BMNEvaluation(opts, dirBmnModels, dirEvents).run();
	}

	private static void init() {
		// Logger.setDebugging(true); // might provide helpful output
		Logger.setPrinting(true);

		double gb = 1024 * 1024 * 1024;
		log("Make sure that your memory limit is increased, using at least 8GB is recommended to process the KaVE datasets...  (-Xmx8G)");
		log("Current max. memory: %.1f GB\n", Runtime.getRuntime().maxMemory() / gb);

		JsonUtilsCcKaveRsseCalls.registerJsonAdapters();
	}

	private static void hibernateExample() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.beginTransaction();

		User user = new User();
		user.setName("user");
		user.setUsername("username");
		user.setToken(UUID.randomUUID().toString());

		session.save(user);
		session.getTransaction().commit();

		ActivityEntry activityEntry = new ActivityEntry(100,200,"Compilation",user);
		session.save(activityEntry);
		session.getTransaction().commit();

		System.out.println("Done example");
	}
};