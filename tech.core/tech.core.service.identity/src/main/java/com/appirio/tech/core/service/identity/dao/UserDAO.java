package com.appirio.tech.core.service.identity.dao;

import java.util.List;

import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

import com.appirio.tech.core.api.v3.TCID;
import com.appirio.tech.core.api.v3.dao.DaoBase;
import com.appirio.tech.core.api.v3.request.FieldSelector;
import com.appirio.tech.core.api.v3.request.QueryParameter;
import com.appirio.tech.core.api.v3.util.jdbi.TCBeanMapperFactory;
import com.appirio.tech.core.service.identity.representation.User;
import com.appirio.tech.core.service.identity.util.Utils;
import com.appirio.tech.core.service.identity.util.idgen.SequenceDAO;
import com.appirio.tech.core.service.identity.util.ldap.LDAPService;
import com.appirio.tech.core.service.identity.util.ldap.MemberStatus;

@UseStringTemplate3StatementLocator
@RegisterMapperFactory(TCBeanMapperFactory.class)
public abstract class UserDAO implements DaoBase<User>, Transactional<UserDAO> {

	@SqlQuery(
			"SELECT u.user_id as id, u.first_name AS firstName, u.last_name AS lastName, u.handle, " +
			"DECODE(u.status, 'A', 1, 0) AS active, " +
			"(SELECT e.address FROM email AS e WHERE e.user_id = :id AND e.email_type_id = 1 AND e.primary_ind = 1) AS email " +
			"FROM user AS u WHERE u.user_id = :id"
	)
	public abstract User findUserById(@Bind("id") long id);

	@SqlQuery(
			"SELECT u.user_id as id, u.first_name AS firstName, u.last_name AS lastName, u.handle, " +
			"DECODE(u.status, 'A', 1, 0) AS active, " +
			"(SELECT e.address FROM user AS u JOIN email AS e ON e.user_id = u.user_id WHERE u.handle_lower = LOWER(:handle) AND e.email_type_id = 1 AND e.primary_ind = 1) AS email " +
			"FROM user AS u WHERE u.handle_lower = LOWER(:handle)"
	)
	public abstract User findUserByHandle(@Bind("handle") String handle);
	
	@SqlQuery(
			"SELECT u.user_id as id, u.first_name AS firstName, u.last_name AS lastName, u.handle, " +
			"DECODE(u.status, 'A', 1, 0) AS active,  " +
			"e.address AS email " +
			"FROM user AS u  JOIN email AS e ON e.user_id = u.user_id WHERE e.address = :email"
	)
	public abstract User findUserByEmail(@Bind("email") String email);

	// TODO: activation code
	@SqlUpdate(
			"INSERT INTO user " +
			"(user_id, first_name, last_name, handle, status, activation_code) VALUES " +
			"(:id, :firstName, :lastName, :handle, :status, 'ABCDEFG')")
			//"(:u.id, :u.firstName, :u.lastName, :u.handle, :u.status, :u.credential.activationCode)")
	public abstract long createUser(@BindBean User user);

	@SqlUpdate(
			"INSERT INTO security_user" +
			"(login_id, user_id, password) VALUES " +
			"(:loginId, :userId, :password)")
	public abstract long createSecurityUser(@Bind("loginId") long loginId, @Bind("userId") String userId, @Bind("password") String password);

	// TODO: how to handle database prefix for table 
	@SqlUpdate(
			"INSERT INTO <database_prefix>coder" +
			"(coder_id, quote, coder_type_id, comp_country_code, display_quote, quote_location, quote_color, display_banner, banner_style) VALUES " +
            "(:coderId, '', null, null,  1, 'md', '#000000', 1, 'bannerStyle4')")
	public abstract long createCoder(
			@Define("database_prefix") String database_prefix, @Bind("coderId") long coderId);

	@SqlUpdate(
			"INSERT INTO email " +
			"(user_id, email_id, email_type_id, address, primary_ind, status_id) VALUES " +
			"(:userId, :emailId, 1, :email, 1, 2)")
	public abstract long registerEmail(@Bind("userId") long userId, @Bind("emailId") long emailId, @Bind("email") String email);
	
	
	private SequenceDAO sequenceDao;
	
	private LDAPService ldapService;
	
	public SequenceDAO getSequenceDao() {
		return sequenceDao;
	}

	public void setSequenceDao(SequenceDAO sequenceDao) {
		this.sequenceDao = sequenceDao;
	}
	
	public LDAPService getLdapService() {
		return ldapService;
	}

	public void setLdapService(LDAPService ldapService) {
		this.ldapService = ldapService;
	}

	@Transaction(TransactionIsolationLevel.READ_COMMITTED)
	public TCID register(User user) {

		Long userId = sequenceDao.nextVal("sequence_user_seq");

		user.setId(new TCID(userId));
		createUser(user);
		createSecurityUser(
			userId, user.getHandle(),
			user.getCredential().encodePassword());
		
		Long emailId = sequenceDao.nextVal("sequence_email_seq");
		registerEmail(userId, emailId, user.getEmail());
		
		// TODO: 
		//createCoder("'informixoltp':", userId);
		
		// TODO:
		// add member to initial groups
		
		registerLDAP(user);
		
		return user.getId();
	}

	public void registerLDAP(User user) {
		if(user==null)
			throw new IllegalArgumentException("user must be specified.");
		
		ldapService.registerMember(Long.parseLong(user.getId().toString()),
									user.getHandle(),
									user.getCredential().getPassword(),
									user.isActive() ? MemberStatus.ACTIVATED : MemberStatus.UNACTIVATED);
	}
	
	@Override
	public List<User> populate(QueryParameter query) throws Exception {
		return null;
	}

	@Override
	public User populateById(FieldSelector selector, TCID id) throws Exception {
		if(id==null || !Utils.isValid(id))
			throw new IllegalArgumentException("Specified id is invalid. id: "+id);
		return findUserById(Utils.toRawValue(id));
	}

	@Override
	public TCID insert(User user) throws Exception {
		return null;
	}

	@Override
	public TCID update(User user) throws Exception {
		return null;
	}

	@Override
	public void delete(TCID id) throws Exception {
	}
	
	public boolean handleExists(String handle) {
		User user = findUserByHandle(handle);
		return user != null;
	}
}
