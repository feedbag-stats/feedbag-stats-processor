package helpers;

import java.util.Collection;

import org.hibernate.SessionFactory;

import aggregation.DeltaImporter;
import aggregation.IDataProcessor;
import aggregation.ImportBatch;
import aggregation.activity.ActivityProcessor;
import aggregation.tdd.TDDProcessor;
import aggregation.various.VariousStatsProcessor;
import cc.kave.commons.model.events.IDEEvent;

public class DataImporter {
	
	
	public static void main(String[] args) {
		SessionFactory factory = HibernateUtil.getSessionFactory();
		DeltaImporter importer = new DeltaImporter(factory);
		IDataProcessor[] processors = {new ActivityProcessor(factory), new TDDProcessor(factory), new VariousStatsProcessor(factory)};

		Collection<IDEEvent> events = DeltaImporter.readEvents("/home/kitty/Desktop/uni/mp/java-cc-kave-examples-master/Events-170301-2/2016-06-10", "10.zip");
		System.out.println(events.size()+" events found");
		ImportBatch batch = new ImportBatch(events);
		
		importer.importData(batch);
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

}
