package api.pojo.responses.all_courses;

import java.util.List;

public class AllCourses {
    private List<Course> courses;

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
}
