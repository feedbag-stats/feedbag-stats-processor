import aggregation.ProcessingManager;
import helpers.HibernateUtil;

public class Start {

	public static void main(String[] args) {
		if(args.length != 2 && args.length != 1) {
			showUse();
			System.exit(0);
		} else if(args[0].equals("add")) {
			add(args[1]);
		} else if(args[0].equals("remove")) {
			remove();
		} else showUse();
		
		
	}
	
	private static void showUse() {
		System.out.println("use: add <full path to zip> | remove\nadd: processes zip\nremove: deletes all data that has been marked for deletion by the user");
	}
	
	private static void add(String file) {
		new ProcessingManager(HibernateUtil.getSessionFactory()).importZip(file);
	}
	
	private static void remove() {
		new ProcessingManager(HibernateUtil.getSessionFactory()).removeMarkedData();
	}

}
