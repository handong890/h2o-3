#!/usr/bin/python
# -*- encoding: utf-8 -*-
import h2o
from h2o.exceptions import H2OTypeError, H2OValueError
from tests import pyunit_utils


def test_rbind_summary():
    df = h2o.H2OFrame([1, 2, 5.5], destination_frame="df") # original frame
    dfr = h2o.H2OFrame([5.5, 1, 2], destination_frame="dfr") # reversed row content
    df1 = df[2, :]
    df2 = df[:2, :]
    summary = df1.summary(return_data=True)
    df3 = df1.rbind(df2) # fixed
    df3r = df2.rbind(df1)
    
    pyunit_utils.compare_frames_local(dfr, df3) # should contain 5.5, 1, 2
    pyunit_utils.compare_frames_local(df, df3r) # should contain 1,2,5.5
    
    df1 = df[3,:] # this will result in an NA since we do not have 4 rows in df.
    dfr[0,0] = float('nan')
    df4 = df1.rbind(df2)
    pyunit_utils.compare_fame_local(df4, dfr) # should contain NA, 1, 2

if __name__ == "__main__":
    pyunit_utils.standalone_test(test_rbind_summary)
else:
    test_rbind_summary()
