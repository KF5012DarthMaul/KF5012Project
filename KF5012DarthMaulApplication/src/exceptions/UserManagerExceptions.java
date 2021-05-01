package exceptions;

public class UserManagerExceptions {
	
    public static class UserAuthenticationFailed extends Exception {
        public UserAuthenticationFailed() {
        }
    }
    public static class UpdateActionFailed extends Exception {
    	public UpdateActionFailed() {
    	}
    	public UpdateActionFailed(String message) {
    		super(message);
    	}
    }
    public static class RemoveActionFailed extends Exception {
    	public RemoveActionFailed() {
    	}
    	public RemoveActionFailed(String message) {
    		super(message);
    	}
    }
    public static class CreateActionFailed extends Exception {
    	public CreateActionFailed() {
    	}
    	public CreateActionFailed(String message) {
    		super(message);
    	}
    }
    public static class ActionFailed extends Exception {
    	public ActionFailed() {
    	}
    	public ActionFailed(String message) {
    		super(message);
    	}
    }
    public static class UserDoesNotExist extends Exception {
    	public UserDoesNotExist() {
    	}
    	public UserDoesNotExist(String message) {
    		super(message);
    	}
    }
}
