package aggregation;

import java.time.LocalDate;

import entity.User;

public interface IDataProcessor {
	public void updateData(ImportBatch batch);
	public void updateDay(User user, LocalDate day);
}
