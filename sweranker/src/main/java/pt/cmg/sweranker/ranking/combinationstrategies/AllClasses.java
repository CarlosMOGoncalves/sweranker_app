package pt.cmg.sweranker.ranking.combinationstrategies;

import java.util.ArrayList;
import java.util.List;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.AnnualClassCombination;

/**
 * This strategy is plainly simple: it basically picks ALL classes of a single year.
 * This means that the resulting combination is really just one list with the ids of all the
 * given degree classes for that year.
 */
public class AllClasses implements ClassCombinationStrategy {


    @Override
    public List<AnnualClassCombination> getAnnualClassCombinations(List<DegreeClass> degreeClassesOfYear) {


        // This is a single combination year, then id will be almost static
        String combinationId = "d_" + degreeClassesOfYear.get(0).getDegreeId() + "_y_" + degreeClassesOfYear.get(0).getYear() + "_c_1";

        AnnualClassCombination singleClassCombination = new AnnualClassCombination(combinationId);
        singleClassCombination.setYear(degreeClassesOfYear.get(0).getYear());

        for (DegreeClass degreeClass : degreeClassesOfYear) {
            singleClassCombination.addDegreeClass(degreeClass.getId());
        }

        List<AnnualClassCombination> annualClassCombinations = new ArrayList<>();
        annualClassCombinations.add(singleClassCombination);

        return annualClassCombinations;
    }

}
