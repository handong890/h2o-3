package hex.tree.xgboost;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import water.*;
import water.fvec.Frame;

public class XGBoostCancelTest extends TestUtil {

    @BeforeClass
    public static void stall() {
        stall_till_cloudsize(1);
    }

    @Test
    public void cancelTrainTest() throws InterruptedException {
        Frame tfr;
        Scope.enter();
        try {
            // Parse frame into H2O
            tfr = Scope.track(parse_test_file("bigdata/laptop/mnist/train.csv.gz"));
            Scope.track(tfr.replace(784, tfr.vecs()[784].toCategoricalVec()));   // Convert response 'C785' to categorical
            DKV.put(tfr);

            // define special columns
            String response = "C785";

            XGBoostModel.XGBoostParameters parms = new XGBoostModel.XGBoostParameters();
            parms._ntrees = 10000; // we never want this to end
            parms._max_depth = 200; // we want trees to train for long time
            parms._train = tfr._key;
            parms._response_column = response;
            parms._seed = 42;

            for (int i = 0; i < 2; i++) { // do this 10 times to increase probability of reproduction
                Job<XGBoostModel> job = new hex.tree.xgboost.XGBoost(parms).trainModel();
                int sleepTime = (int) (1000 * (2 + 5 * Math.random()));
                System.out.println("Going to sleep for " + (sleepTime / 1000) + "sec.");
                Thread.sleep(sleepTime); // sleep a random number of time
                job.stop(); // on single node this always passes, on multi node this used to crash the cluster
                try {
                    job.get();
                } catch (Job.JobCancelledException e) {
                    // expected
                }
                if (job._result != null) {
                    ((XGBoostModel) DKV.get(job._result).get()).remove();
                }
            }
        } finally {
            Scope.exit();
        }
    }

}
