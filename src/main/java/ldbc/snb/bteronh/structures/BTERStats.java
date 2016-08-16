package ldbc.snb.bteronh.structures;

import java.util.Arrays;

/**
 * Created by aprat on 14/07/16.
 */
public class BTERStats {

    int maxDegree = Integer.MIN_VALUE;

    //Phase 1 data
    int [] i_g = null; //index of group in degree sequence
    int [] n_g = null; //number of buckets in group
    int [] b_g = null; //size of bucket in group
    double [] w_g = null; //weight of buckets in group

    //Phase 2 data
    int [] nfill_d = null;
    int [] nbulk_d = null;
    double [] wfill_d = null;
    double [] wbulk_d = null;
    double [] w_d = null;
    double [] r_d = null;
    int [] i_d = null;
    int [] n_d = null;


    double [] cumulativeGroups = null;
    double [] cumulativeDegrees = null;
    int weightPhase1;
    int weightPhase2;

    public void initialize(int [] degreeSequence, double [] ccPerDegree) {

        maxDegree = Integer.MIN_VALUE;
        for(int i = 0; i < degreeSequence.length; ++i) {
           maxDegree = maxDegree < degreeSequence[i] ? degreeSequence[i] : maxDegree;
        }

        i_g = new int[maxDegree+1];
        b_g = new int[maxDegree+1];
        n_g = new int[maxDegree+1];
        w_g = new double[maxDegree+1];
        nfill_d = new int[maxDegree+1];
        nbulk_d = new int[maxDegree+1];
        wfill_d = new double[maxDegree+1];
        wbulk_d = new double[maxDegree+1];
        w_d = new double[maxDegree+1];
        r_d = new double[maxDegree+1];
        n_d = new int[maxDegree+1];
        i_d = new int[maxDegree+1];
        for(int i = 0; i < maxDegree+1; ++i) {
            i_g[i] = 0;
            b_g[i] = 0;
            n_g[i] = i+1;
            w_g[i] = 0;

            nfill_d[i] = 0;
            nbulk_d[i] = 0;
            wfill_d[i] = 0;
            wbulk_d[i] = 0;
            w_d[i] = 0;
            r_d[i] = 0;
            i_d[i] = 0;
            n_d[i] = 0;
        }

        // initializing n_d
        for(int i = 0; i < degreeSequence.length; ++i) {
            n_d[degreeSequence[i]]++;
        }

        // initializing i_d
        i_d[2] = 0;
        for(int i = 3; i <= maxDegree; ++i) {
            i_d[i] = i_d[i-1] + n_d[i-1];
        }
        i_d[1] = i_d[maxDegree] + n_d[maxDegree];
        i_g[1] = i_d[maxDegree] + n_d[maxDegree];

        int [] n_dPrima = new int[maxDegree+1];
        int accum = 0;
        for(int i = 0; i <= maxDegree; ++i) {
            accum+=n_d[i];
            n_dPrima[i]=degreeSequence.length-accum;
        }

        //handling degree 1 nodes
        nfill_d[1] = 10*n_d[1];
        wfill_d[1] = 0.5*n_d[1];
        wbulk_d[1] = 0;
        w_d[1] = wbulk_d[1] + wfill_d[1];
        r_d[1] = 1;

        // Initializing group arrays.
        int nFillPrevious = 0;
        int g = -1;
        double dInternalPrevious = 0;
        for(int i = 2; i <= maxDegree; ++i) {
            if(nFillPrevious > 0) {
                nfill_d[i] = Math.min(nFillPrevious, n_d[i]);
                nFillPrevious-=nfill_d[i];
                wfill_d[i] = 0.5*nfill_d[i]*(i-dInternalPrevious);
            } else {
                nfill_d[i] = 0;
                wfill_d[i] = 0;
            }
            nbulk_d[i] = n_d[i] - nfill_d[i];
            if(nbulk_d[i] > 0) {
                g+=1;
                i_g[g] = i_d[i] + nfill_d[i];
                b_g[g] = (int)Math.ceil(nbulk_d[i]/(double)(i+1));
                n_g[g] = i+1;
                if(b_g[g]*(i+1) > (n_dPrima[i]+nbulk_d[i])) {
                    n_g[g] = (n_dPrima[i]+nbulk_d[i]);
                }
                double p = Math.pow(ccPerDegree[i],1/3.0);
                dInternalPrevious = (n_g[g]-1)*p;
                wbulk_d[i] = 0.5*nbulk_d[i]*(i-dInternalPrevious);
                w_g[g] = b_g[g]*0.5*n_g[g]*(n_g[g]-1)*Math.log(1/(1.0-p));
                nFillPrevious = (b_g[g]*n_g[g]) - nbulk_d[i];
            } else {
                wbulk_d[i] = 0;
            }
            w_d[i] = wfill_d[i] + wbulk_d[i];
            r_d[i] = wfill_d[i] / w_d[i];
        }

        int [] newi_g = new int[g+1];
        int [] newn_g = new int[g+1]; //number of buckets in group
        int [] newb_g = new int[g+1]; //size of bucket in group
        double [] neww_g = new double[g+1]; //weight of buckets in group
        for(int i = 0; i < g+1; ++i) {
            newi_g[i] = i_g[i];
            newn_g[i] = n_g[i];
            newb_g[i] = b_g[i];
            neww_g[i] = w_g[i];
        }

        i_g = newi_g;
        n_g = newn_g;
        b_g = newb_g;
        w_g = neww_g;

        weightPhase1 = 0;
        weightPhase2 = 0;
        for(int i = 0; i < getNumGroups(); ++i) {
            weightPhase1+= getGroupWeight(i);
        }

        for(int i = 0; i < (getMaxDegree()+1); ++i) {
            weightPhase2+=getDegreeWeight(i);
        }

        cumulativeGroups = new double[getNumGroups()];
        cumulativeGroups[0] = getGroupWeight(0) / (double)weightPhase1;
        for(int i = 1; i < getNumGroups(); ++i) {
            cumulativeGroups[i] = Math.min(cumulativeGroups[i-1] + getGroupWeight(i) / (double)weightPhase1, 1.0);
        }

        cumulativeDegrees = new double[getMaxDegree()+1];
        cumulativeGroups[0] = getDegreeWeight(0) / (double)weightPhase2;
        for(int i = 1; i < (getMaxDegree()+1); ++i) {
            cumulativeDegrees[i] = Math.min(cumulativeDegrees[i-1] + getDegreeWeight(i) / (double)weightPhase2,1.0);
        }
    }

    public int getGroupIndex(int group) {return i_g[group];}

    public int getGroupBucketSize(int group) {return n_g[group];}

    public int getGroupNumBuckets(int group) {return b_g[group];}

    public double getGroupWeight(int group) {return w_g[group];}

    public double [] getGroupWeights() { return w_g;}

    public int getDegreeIndex(int degree) { return i_d[degree];}

    public int getDegreeNFill(int degree) { return nfill_d[degree];}

    public int getDegreeNBulk(int degree) { return nbulk_d[degree];}

    public double getDegreeWFill(int degree) { return wfill_d[degree];}

    public double getDegreeWBulk(int degree) { return wbulk_d[degree];}

    public double getDegreeWeight(int degree) { return w_d[degree];}

    public double [] getDegreeWeights() { return w_d;}

    public double getDegreeWeightRatio( int degree ) { return r_d[degree];}

    public double [] getDegreeWeightRatios() { return r_d;}

    public double getDegreeN(int degree) { return n_d[degree];}

    public int getNumGroups() {
        return n_g.length;
    }

    public int getMaxDegree() { return maxDegree;}

    public double[] getCumulativeGroups() {
        return cumulativeGroups;
    }

    public double[] getCumulativeDegrees() {
        return cumulativeDegrees;
    }

    public int getWeightPhase1() {
        return weightPhase1;
    }

    public int getWeightPhase2() {
        return weightPhase2;
    }

}
