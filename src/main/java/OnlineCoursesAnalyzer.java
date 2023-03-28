import org.w3c.dom.css.CSSStyleDeclaration;

import javax.lang.model.element.NestingKind;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> helper = new HashMap<>();
        for (Course a : courses) {
            if (helper.containsKey(a.institution)) {
                helper.put(a.institution, helper.get(a.institution) + a.participants);
            } else {
                helper.put(a.institution, a.participants);
            }
        }
        Map<String, Integer> answer = new LinkedHashMap<>();
        helper.entrySet().stream()
                .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .forEach(stringIntegerEntry -> answer.put(stringIntegerEntry.getKey(), stringIntegerEntry.getValue()));
        return answer;

    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> helper = new HashMap<>();
        for (Course a : courses) {
            String string = a.institution + "-" + a.subject;
            if (helper.containsKey(string)) {
                helper.put(string, helper.get(string) + a.participants);
            } else {
                helper.put(string, a.participants);
            }
        }

        Map<String, Integer> answer = new LinkedHashMap<>();
        helper.entrySet().stream()
                .sorted((o1, o2) -> -o1.getValue().compareTo(o2.getValue()))
                .forEach(stringIntegerEntry -> {
                    answer.put(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());
                });


        return answer;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> answer = new LinkedHashMap<>();

        courses.forEach(course -> {
            String[] singleIns = course.instructors.split(",");
            if (singleIns.length == 0) {
                return;
            }
            for (String string : singleIns) {
                string = string.trim();
                if (answer.containsKey(string)) {
                    if (singleIns.length == 1 && !answer.get(string).get(0).contains(course.title)) {
                        answer.get(string).get(0).add(course.title);
                    } else if (singleIns.length != 1 && !answer.get(string).get(1).contains(course.title)) {
                        answer.get(string).get(1).add(course.title);
                    }
                } else {
                    List<List<String>> helper = new ArrayList<>();
                    List<String> ls1 = new ArrayList<>(), ls2 = new ArrayList<>();
                    helper.add(ls1);
                    helper.add(ls2);
                    if (singleIns.length == 1) {
                        helper.get(0).add(course.title);
                    } else {
                        helper.get(1).add(course.title);
                    }
                    answer.put(string, helper);
                }
            }
        });


        answer.forEach((key, value) -> Collections.sort(value.get(0)));
        answer.forEach((key, value) -> Collections.sort(value.get(1)));
        return answer;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        List<String> answer = new ArrayList<>();
        switch (by) {
            case "hours":
                Map<String, Double> title_hours = new HashMap<>();
                for (Course course : courses) {
                    if (title_hours.containsKey(course.title)) {
                        if(course.totalHours>title_hours.get(course.title)){
                            title_hours.put(course.title,course.totalHours);
                        }
                    } else {
                        title_hours.put(course.title, course.totalHours);
                    }
                }

                title_hours.entrySet().stream()
                        .sorted((o1, o2) -> {
                            int a = -o1.getValue().compareTo(o2.getValue());
                            if (a == 0) {
                                a = o1.getKey().compareTo(o2.getKey());
                            }
                            return a;
                        })
                        .limit(topK)
                        .forEach(stringDoubleEntry -> answer.add(stringDoubleEntry.getKey()));
                break;
            case "participants":
                Map<String, Integer> title_pars = new HashMap<>();
                for (Course course : courses) {
                    if (title_pars.containsKey(course.title)) {
                        if(course.participants>title_pars.get(course.title)){
                            title_pars.put(course.title,course.participants);
                        }
                    } else {
                        title_pars.put(course.title, course.participants);
                    }
                }

                title_pars.entrySet().stream()
                        .sorted((o1, o2) -> {
                            int a = -o1.getValue().compareTo(o2.getValue());
                            if (a == 0) {
                                a = o1.getKey().compareTo(o2.getKey());
                            }
                            return a;
                        })
                        .limit(topK)
                        .forEach(stringDoubleEntry -> answer.add(stringDoubleEntry.getKey()));
                break;
            default:
                break;
        }
        return answer;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        return null;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        return null;
    }

}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}