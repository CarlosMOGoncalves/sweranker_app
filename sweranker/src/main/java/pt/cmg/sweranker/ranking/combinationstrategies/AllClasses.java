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

    private static int currentYear;

    @Override
    public List<AnnualClassCombination> getAnnualClassCombinations(int year, List<DegreeClass> allDegreeClasses) {

        currentYear = year;

        // This is a single combination year, then id will be almost static
        String combinationId = "d_" + allDegreeClasses.get(0).getDegreeId() + "_y_" + year + "_c_1";

        AnnualClassCombination singleClassCombination = new AnnualClassCombination(combinationId);
        singleClassCombination.setYear(year);

        List<DegreeClass> degreeClassesOfYear = getDegreeClassesOfYear(allDegreeClasses);

        for (DegreeClass degreeClass : degreeClassesOfYear) {
            singleClassCombination.addDegreeClass(degreeClass.getId());
        }

        List<AnnualClassCombination> annualClassCombinations = new ArrayList<>();
        annualClassCombinations.add(singleClassCombination);

        return annualClassCombinations;
    }


    /**
     * This function pretty much just returns a list with all the Degree Classes for THE CURRENT YEAR
     * we are trying to get the combinations for.
     */
    private List<DegreeClass> getDegreeClassesOfYear(List<DegreeClass> allDegreeClasses) {
        List<DegreeClass> classesOfYear = new ArrayList<>();
        for (DegreeClass degreeClass : allDegreeClasses) {
            if (degreeClass.getYear() == currentYear) {
                classesOfYear.add(degreeClass);
            }
        }
        return classesOfYear;
    }

}
