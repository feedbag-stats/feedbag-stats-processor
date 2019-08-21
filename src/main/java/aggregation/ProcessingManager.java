package aggregation;
import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.SessionFactory;

import aggregation.DataRemover;
import aggregation.DeltaImporter;
import aggregation.IDataProcessor;
import aggregation.ImportBatch;
import aggregation.activity.ActivityProcessor;
import aggregation.location.LocationProcessor;
import aggregation.tdd.TDDProcessor;
import aggregation.various.VariousStatsProcessor;
import cc.kave.commons.model.events.IDEEvent;
import entity.User;

public class ProcessingManager {
	
	private final DeltaImporter importer;
	private final DataRemover remover;
	private Collection<IDataProcessor> processors = new ArrayList<>();

	public ProcessingManager(SessionFactory factory) {
		importer = new DeltaImporter(factory);
		remover = new DataRemover(factory);
		processors.add(new ActivityProcessor(factory));
		processors.add(new TDDProcessor(factory));
		processors.add(new VariousStatsProcessor(factory)); //add after ActivityProcessor
		processors.add(new LocationProcessor(factory)); //add after ActivityProcessor
	}
	
	public void importZip(String zip) {
		Collection<IDEEvent> events = DeltaImporter.readEvents(zip);
		if(events==null) {
			System.out.println("Zip not found: "+zip);
			return;
		}
		System.out.println(events.size()+" events found");
		ImportBatch batch = new ImportBatch(events);
		if(!batch.batchIsValid()) {
			System.out.println("Zip "+zip+" is not a valid import batch. Needs username, at least 1 event.");
			return;
		}
		
		importer.importData(batch, zip);
		System.out.println("Imported Data");
		for(IDataProcessor p : processors) {
			try {
				p.updateData(batch);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				System.out.println(p.getClass().toString()+" is done processing.");
			}
		}
		
		System.out.println("Done importing!");
	}
	
	public void removeMarkedData() {
		Collection<User> users = remover.findUsersAffectedByRemoval();
		if (users.isEmpty()) {
			return;
		}
		Collection<String> zipsToReimport = remover.findAllZipsForUsers(users);
		remover.deleteZipsToDelete();
		remover.deleteAllDataForUsers(users);
		for(String zip : zipsToReimport) {
			importZip(zip);
		}
	}

}
