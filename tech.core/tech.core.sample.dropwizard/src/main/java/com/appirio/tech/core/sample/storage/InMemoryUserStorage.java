/**
 * 
 */
package com.appirio.tech.core.sample.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.request.FilterParameter;
import com.appirio.tech.core.sample.exception.StorageException;
import com.appirio.tech.core.sample.representation.Sample;


/**
 * @author sudo
 *
 */
public class InMemoryUserStorage {
	
	private static InMemoryUserStorage instance = new InMemoryUserStorage();
	
	private AtomicInteger userDual = new AtomicInteger(1000);
	
	private List<Sample> userList = new ArrayList<Sample>();
	
	/**
	 * Singleton In-memory Storage.
	 * Use {@link #instance()} to obtain the instance.
	 */
	private InMemoryUserStorage() {	
		createSample();
	}
	
	private void createSample() {
		insertUser("test", "test@appirio.com", "FirstTest", "LastTest");
		insertUser("rockabilly", "bglass@appirio.com", "Bryce", "Glass");
		insertUser("aqmansuri", "abdul.mansoori@appirio.com", "Abdul", "Masoori");
		insertUser("anjali.jain", "anjali.jain@appirio.com", "Anjali", "Jain");
		insertUser("bruzzi", "cbruzzi@appirio.com", "Chris", "Bruzzi");
		insertUser("jamestc", "james@appirio.com", "James", "Eitzmann");
		insertUser("michaelpress", "mpress@appirio.com", "Michael", "Press");
		insertUser("CloudSurfer", "rjain@appirio.com", "Rohit", "Jain");
		insertUser("mashannon168", "sma@appirio.com", "Shannon", "Ma");
		insertUser("thx1138", "thaas@topcoder.com", "Travis", "Haas");
		insertUser("TonyJ", "tjefts@appirio.com", "Tony", "Jefts");
		insertUser("Dumbfire", "will@appirio.com", "Will", "Supinski");
		insertUser("kohata", "ykohata@appirio.com", "Yoshifumi", "Kohata");
		insertUser("sudo0124", "sudo@appirio.com ", "Yoshito", "Sudo");
		insertUser("_indy", "nhastings@appirio.com", "Neil", "Hastings");
		insertUser("ashish06feb3", "ashish06feb3@gmail.com", "Ashish", "Agarwal");
		insertUser("PMath", "kymathur@gmail.com", "Puneet", "Mathur");
		insertUser("tladendo", "tladendorf@appirio.com", "Tom", "Ladendorf");
		insertUser("dayal", "dayal@appirio.com", "Dayal", "Gaitonde");
		insertUser("DhananjayKumar1", "dhananjay.kumar@appirio.com", "Dhananjay", "Kumar");
	}

	private Sample insertUser(String handle, String email, String firstName, String lastName) {
		Sample user = new Sample();
		user.setHandle(handle);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		return insertUser(user);
	}

	public static InMemoryUserStorage instance() {
		return instance;
	}
	
	public Sample insertUser(Sample user) {
		user.setId(new TCID(userDual.getAndIncrement()));
		user.setCreatedAt(new DateTime());
		user.setModifiedAt(new DateTime());
		userList.add(user);
		return user;
	}
	
	public void deleteUser(TCID id) {
		for(Sample user: userList) {
			if(user.getId().equals(id)) {
				userList.remove(user);
				return;
			}
		}
		throw new StorageException("Record Not Found:" + id);
	}
	
	public List<Sample> getUserList() {
		return userList;
	}

	public List<Sample> getFilteredUserList(FilterParameter parameter) {
		List<Sample> resultList = new ArrayList<Sample>();
		
		//Filter to specified queries
		for(Sample user : getUserList()) {
			if(parameter.contains("handle") && !(parameter.get("handle").equals(user.getHandle()))) continue;
			if(parameter.contains("email") && !(parameter.get("email").equals(user.getEmail()))) continue;
			if(parameter.contains("firstName") && !(parameter.get("firstName").equals(user.getFirstName()))) continue;
			if(parameter.contains("lastName") && !(parameter.get("lastName").equals(user.getLastName()))) continue;
			resultList.add(user);
		}
		return resultList;
	}

	/**
	 * @param object
	 */
	public void updateUser(Sample object) {
		TCID id = object.getId();
		Sample orgUser = null;
		for(Sample user : getUserList()) {
			if(user.getId().equals(id)) {
				orgUser = user; break;
			}
		}
		if(orgUser==null) {
			throw new StorageException("Id of the Sample not found:" + id);
		}
		orgUser.setHandle(object.getHandle());
		orgUser.setEmail(object.getEmail());
		orgUser.setFirstName(object.getFirstName());
		orgUser.setLastName(object.getLastName());
		orgUser.setModifiedAt(object.getModifiedAt());
	}
	
}
