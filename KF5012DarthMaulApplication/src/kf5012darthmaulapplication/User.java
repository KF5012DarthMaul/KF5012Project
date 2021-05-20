package kf5012darthmaulapplication;

/**
 * 
 * @author Scrub
 *
 */

public class User {
	String username;
    String displayName;
	int encodedPermission;
	public PermissionManager pm;
	PermissionManager.AccountType accountType;
	
	/**
	 * @param username
	 * @param accountType
	 * @throws Exception
	 */
	public User(String username, String name, PermissionManager.AccountType accountType){
		this.username = username;
                this.displayName = name;
		this.accountType = accountType;
		pm = new PermissionManager(this.accountType);
	}
	/**
	 * Gets User Username
	 * @return string
	 */
	public String getUsername() {
		return this.username;
	}
        
        public String getDisplayName(){
            return this.displayName;
        }
        
        public void setDisplayName(String name)
        {
            this.displayName = name;
        }
        
	/**
	 * Gets User AccountType
	 * @return PermissionManager.AccountType
	 */
	public PermissionManager.AccountType getAccountType(){
		return this.accountType;
	}
}
