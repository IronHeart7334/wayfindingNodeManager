package nodemanager.io;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Since interacting with the Google Drive may result in permission problems,
 * and threads can get a little weird with throwing errors, we need an interface to work with them
 * 
 * A DriveIOOp allows us to keep track of an interaction with the Google Drive.
 * This interaction is defined in the perform() method. 
 * execute() returns a thread which runs the perform() method.
 * If perform() succeeds, passes the value returned from it to each onSucceed function
 * if it fails, passes the Exception to each onFail function.
 * 
 * @author Matt Crow
 */
public abstract class DriveIOOp {
    private final ArrayList<Consumer<Exception>> onFail;
    private final ArrayList<Consumer<Object>> onSucceed;
    private Exception alreadyFailed; //just in case the thread is already done
    private Object alreadySucceeded;
    private Thread t;
    
    public DriveIOOp(){
        onFail = new ArrayList<>();
        onSucceed = new ArrayList<>();
        onFail.add((e)->e.printStackTrace());
    }
    
    
    /**
     * Make sure you call this part!
     * If execute hasn't been called yet, starts a new thread, which runs this' perform() method
     * @return the thread running this' perform() method
     */
    public final Thread execute(){
        if(t == null){
            t = new Thread(){
                @Override
                public void run(){
                    try {
                        alreadySucceeded = perform();
                        onSucceed.forEach((func)->func.accept(alreadySucceeded));
                    } catch (Exception ex) {
                        alreadyFailed = ex;
                        onFail.forEach((func)->func.accept(ex));
                    }
                }
            };
            t.start();
        }
        return t;
    }
    
    /**
     * Adds a function which will accept the result of perform() if it succeeds.
     * If this had already succeeded, immediately passes the value of that success to the function
     * @param func a function to run upon successfully running perform()
     * @return this, for chaining purposes
     */
    public final DriveIOOp addOnSucceed(Consumer<Object> func){
        onSucceed.add(func);
        if(alreadySucceeded != null){
            func.accept(alreadySucceeded);
        }
        return this;
    }
    
    /**
     * Adds a function which will accept the result of perform() if it fails.
     * If this had already failed, immediately passes the value of that failure to the function
     * @param func a function to run upon successfully running perform()
     * @return this, for chaining purposes
     */
    public final DriveIOOp addOnFail(Consumer<Exception> func){
        onFail.add(func);
        if(alreadyFailed != null){
            func.accept(alreadyFailed);
        }
        return this;
    }
    
    /**
     * DriveIOOp runs the contents of method asynchronously upon calling .execute()
     * @return what you want to pass to success functions
     * @throws Exception what you want to pass to failure functions
     */
    public abstract Object perform() throws Exception;
}