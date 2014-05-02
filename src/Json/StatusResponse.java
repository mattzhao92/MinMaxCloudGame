package Json;

/**
 * Packet for sending a status message
 *
 */
public class StatusResponse {

	/**
	 * The status
	 */
	public String status;
	
	/**
	 * Any accompanying message for the status
	 */
	public String msg;
	
	/**
	 * Constructor
	 * @param statusIn
	 * @param msgIn
	 */
	public StatusResponse(String statusIn, String msgIn) {
		this.status = statusIn;
		this.msg = msgIn;
	}
	
}
