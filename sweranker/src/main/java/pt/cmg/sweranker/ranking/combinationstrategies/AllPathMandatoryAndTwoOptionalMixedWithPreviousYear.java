package pt.cmg.sweranker.ranking.combinationstrategies;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pt.cmg.sweranker.degrees.DegreeClass;
import pt.cmg.sweranker.ranking.AnnualClassCombination;

/**
 * Created by Carlos on 22/09/2017.<br/><br/>
 * <p>
 * <p>
 * This is slightly more complicated algorithm class to calculate the Degree Class Annual Combinations.
 * Originally developed for the Degree (Universidade de Coimbra - LEI + MEI), this is a departure from the
 * simpler ones previously developed for the (Universidade do Porto - MiEIC), in the way that this algorithm
 * was the first developed to deal with Multi Path Degree Years.
 * <p>
 * While most degrees have a straightforward structure, with a common trunk of classes in the first years
 * and then the more advanced years with mandatory classes plus a choice of sme optionals, there are those
 * like Coimbra and Minho that split their advanced years into "Paths". These are nothing more than pre-built
 * sets of classes of a given theme that must be taken together. And these paths are likely mutually exclusive,
 * i.e. if you take one, you cannot take another.
 * </p>
 * <p>
 * However, Coimbra goes even further by adding some optional classes to the pre-built roaster. And that
 * really just means that the way to calculate the annual combinations gets really complicated.
 * </p>
 * <p>
 * In particular, this algorithm class assumes the following:<br/>
 * 1 - This year has multiple paths, it can be anything upwards of 1 <br/>
 * 2 - Each path, for that year, has mandatory classes shared by every path <br/>
 * 3 - Each path, for that year, has optional classes that are shared among every path <br/>
 * 4 - Each path, for that year, has optional classes FROM THE PREVIOUS YEAR and of THE 1ST SEMESTER
 * that were mandatory in another path, but that can now be used as optionals in the current year for
 * the current path.<br/>
 * 5 - The number of optional classes that can be added to the mandatory ones is TWO!
 * <p>
 * </p>
 */
public class AllPathMandatoryAndTwoOptionalMixedWithPreviousYear implements ClassCombinationStrategy {

    private static String combinationIdBase;
    private static int idCounter;
    private static int currentYear;

    @Override
    public List<AnnualClassCombination> getAnnualClassCombinations(int year, List<DegreeClass> allDegreeClasses) {

        currentYear = year;
        combinationIdBase = "d_" + allDegreeClasses.get(0).getDegreeId() + "_y_" + year + "_c_";
        idCounter = 1;

        List<AnnualClassCombination> allAnnualCombinations = new ArrayList<>();
        List<DegreeClass> classesOfYear = getDegreeClassesOfYear(currentYear, allDegreeClasses);

        Set<Integer> availablePaths = getAvailablePaths(classesOfYear);

        for (Integer path : availablePaths) {

            List<String> mandatoryClasses = getMandatoryPathClasses(path, classesOfYear);
            List<String> optionalClasses = getOptionalPathClasses(path, classesOfYear, allDegreeClasses);

            allAnnualCombinations.addAll(generateAnnualCombinations(mandatoryClasses, optionalClasses));

        }

        return allAnnualCombinations;
    }

    /**
     * This function pretty much just returns a list with all the Degree Classes for THE CURRENT YEAR
     * we are trying to get the combinations for.
     */
    private List<DegreeClass> getDegreeClassesOfYear(int year, List<DegreeClass> allDegreeClasses) {
        List<DegreeClass> classesOfYear = new ArrayList<>();
        for (DegreeClass degreeClass : allDegreeClasses) {
            if (degreeClass.getYear() == year) {
                classesOfYear.add(degreeClass);
            }
        }
        return classesOfYear;
    }

    /**
     * Returns the Set with all the available paths for this year's classes.
     * Some degrees have different paths to complete it and this is an approach to it.
     *
     * @param allClasses
     * @return
     */
    private Set<Integer> getAvailablePaths(List<DegreeClass> allClasses) {

        Set<Integer> paths = new TreeSet<>();
        // Atenção a esta instrução, isto vai os arrays de todas as cadeiras do ano para inserir multiplas vezes
        // os mesmos paths, como é um SET não haverá repetidos, mas mesmo assim vai tudo lá para dentro
        for (DegreeClass degreeClass : allClasses) {
            if (degreeClass.hasPaths()) {
                for (int i = 0; i < degreeClass.getPaths().length; i++) {
                    paths.add(degreeClass.getPaths()[i]);
                }
            }
        }
        return paths;
    }

    /**
     * Returns a list with all the MANDATORY classes for the given PATH.
     * This is very important, because the choosing of these classes are determinant
     * to the combinations generated.
     */
    private List<String> getMandatoryPathClasses(int path, List<DegreeClass> classesOfYear) {

        List<String> mandatoryClasses = new ArrayList<>();
        for (DegreeClass degreeClass : classesOfYear) {
            if (degreeClass.isMandatoryClass()) {
                mandatoryClasses.add(degreeClass.getId());
            }

            // This is not really necessary as in Coimbra there are no path mandatory in the 5th year
            if (degreeClass.isPathMandatory(path)) {
                mandatoryClasses.add(degreeClass.getId());
            }
        }
        return mandatoryClasses;
    }


    /**
     * Returns the optional classes for this path. This is normally slightly more complicated in
     * PATH aware degrees because some classes need to be picked here and there.
     * In any case this is the list that will suffer Combinatory math a bit further down the line
     * to effectively create the final combinations of the year.
     */
    private List<String> getOptionalPathClasses(int path, List<DegreeClass> classesOfTargetYear, List<DegreeClass> allDegreeClasses) {

        List<String> optionalClasses = new ArrayList<>();

        // First get the optionals unique of the current year
        for (DegreeClass degreeClass : classesOfTargetYear) {
            if (degreeClass.isOptionalForPath(path)) {
                optionalClasses.add(degreeClass.getId());
            }
        }
        // Then get the optionals that could be used from the previous year
        optionalClasses.addAll(getPreviousYearOptionals(path, allDegreeClasses));

        return optionalClasses;
    }


    /**
     * Very very tricky.
     * This one get the classes of the previous year, because the remaining optionals are there.
     * But that is not all.
     * It fetches only those of the 1st Semester, because this year needs optionals only from the 1st Semester
     * and then, even more macabre, it fetches only those that are mandatory to EVERY OTHER PATH than the one
     * we are dealing with. Again, because the remaining optionals of this year, are the ones of the previous
     * that belong to the 1st semester AND are mandatory to OTHER paths.
     * <p>
     * Yeah, pretty dark stuff.
     */
    private List<String> getPreviousYearOptionals(int path, List<DegreeClass> allDegreeClasses) {

        List<String> optionalsOfPreviousYear = new ArrayList<>();

        List<DegreeClass> classesOfPreviousYear = getDegreeClassesOfYear(currentYear - 1, allDegreeClasses);

        for (DegreeClass degreeClass : classesOfPreviousYear) {
            if (degreeClass.getSemester() == 1 && degreeClass.hasMandatoryPaths() && !degreeClass.isPathMandatory(path)) {
                optionalsOfPreviousYear.add(degreeClass.getId());
            }
        }
        return optionalsOfPreviousYear;

    }


    private List<AnnualClassCombination> generateAnnualCombinations(List<String> mandatoryClasses, List<String> optionalClasses) {

        ICombinatoricsVector<String> optionalVector = Factory.createVector(optionalClasses.toArray(new String[optionalClasses.size()]));

        // creates the generator that can calculate the needed combinations.
        Generator<String> optionalCombinations = Factory.createSimpleCombinationGenerator(optionalVector, 2);


        List<AnnualClassCombination> resultingCombinations = new ArrayList<>();


        // Now iterate over all the possible combinations, add them the mandatory classes and add them to the final object.
        for (ICombinatoricsVector<String> comboVector : optionalCombinations.generateAllObjects()) {

            AnnualClassCombination currentCombination = new AnnualClassCombination(combinationIdBase + idCounter);
            currentCombination.setYear(currentYear);

            currentCombination.addDegreeClasses(comboVector.getVector());
            currentCombination.addDegreeClasses(mandatoryClasses);

            resultingCombinations.add(currentCombination);

            // increment counter to create a new ID for this particular combo
            idCounter++;
        }

        return resultingCombinations;
    }

}
