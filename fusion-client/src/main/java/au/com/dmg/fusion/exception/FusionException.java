package au.com.dmg.fusion.exception;

public class FusionException extends RuntimeException {

	private static final long serialVersionUID = 3906348029921710950L;

	public boolean ErrorRecoveryRequired = true;

	public FusionException(String message, boolean errorRecoveryRequired)
	{
		super(message);
		ErrorRecoveryRequired = errorRecoveryRequired;
	}

	public FusionException(String message, Throwable cause) {
		super(message, cause);
	}

	public FusionException(String message) {
		super(message);
	}

	public FusionException(Throwable cause) {
		super(cause);
	}

}
