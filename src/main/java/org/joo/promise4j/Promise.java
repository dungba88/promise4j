package org.joo.promise4j;

public interface Promise<D, F extends Throwable> {
    
    public Promise<D, F> done(DoneCallback<D> callback);
    
    public Promise<D, F> fail(FailCallback<F> callback);
    
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeDone(PipeDoneCallback<D, D_OUT, F_OUT> callback);
    
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeFail(PipeFailureCallback<F, D_OUT, F_OUT> failCallback);
}
