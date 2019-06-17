package entity;

import javax.persistence.*;

@Entity
@Table(name="EditLocation")
public class EditLocation {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(nullable = false)
	private String solution;
	
	@Column(nullable = false)
	private String project;
	
	@Column(nullable = false)
	private String package1;
	
	@Column(nullable = false)
	private String file;
	
	public EditLocation(String solution, String project, String packageName, String file) {
		this.solution = solution;
		this.project = project;
		this.package1 = packageName;
		this.file = file;
	}

	public String getSolution() {
		return solution;
	}

	public String getProject() {
		return project;
	}

	public String getPackage() {
		return package1;
	}

	public String getFile() {
		return file;
	}
	
	public String toString() {
		return solution+"."+project+"."+package1+"."+file;
	}
}
