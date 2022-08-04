package com.flab.service;

import com.flab.exception.NoSuchCourseException;
import com.flab.exception.NoSuchScoreException;
import com.flab.exception.NoSuchStudentException;
import com.flab.model.Course;
import com.flab.model.Score;
import com.flab.model.Student;
import com.flab.repository.CourseRepository;
import com.flab.repository.ScoreRepository;
import com.flab.repository.StudentRepository;
import com.flab.service.impl.TranscriptServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class TranscriptServiceImplTest {

    private static final Course KOREAN = new Course().setId(1).setName("korean");
    private static final Course ENGLISH = new Course().setId(2).setName("english");
    private static final Course MATH = new Course().setId(3).setName("math");
    private static final Course SCIENCE = new Course().setId(4).setName("science");

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private StudentRepository studentRepository;

    @Spy
    @InjectMocks
    private TranscriptServiceImpl transcriptService;


    @Test
    void testGetAverageScore_HappyCase_VerifyReturnedValue_Success() {
        // given
        final int studentID = 1;
        final Student trey = new Student().setId(studentID).setName("Trey").setMajor("Computer Engineering")
                .setCourses(List.of(KOREAN, ENGLISH, MATH, SCIENCE));

        Mockito.when(studentRepository.getStudent(1))
                .thenReturn(Optional.of(trey));

        Mockito.when(scoreRepository.getScore(studentID, 1))
                .thenReturn(Optional.of(new Score().setCourse(KOREAN).setScore(100)));
        Mockito.when(scoreRepository.getScore(studentID, 2))
                .thenReturn(Optional.of(new Score().setCourse(ENGLISH).setScore(90)));
        Mockito.when(scoreRepository.getScore(studentID, 3))
                .thenReturn(Optional.of(new Score().setCourse(MATH).setScore(80)));
        Mockito.when(scoreRepository.getScore(studentID, 4))
                .thenReturn(Optional.of(new Score().setCourse(SCIENCE).setScore(70)));

        // when
        final double averageScore = transcriptService.getAverageScore(studentID);

        // then
        Assertions.assertEquals(85.0, averageScore);
    }

    @Test
    @DisplayName("주어진 studentID에 해당되는 Student가 없을 때, getAverageScore()는 NoSuchStudentException을 Throw 한다.")
    void testGetAverageScore_StudentNotExist_ThrowNoSuchStudentException_Error() {
        // given
        final int studentID = 1;
        Mockito.when(studentRepository.getStudent(studentID))
                .thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(NoSuchStudentException.class, () -> transcriptService.getAverageScore(studentID));
    }

    @Test
    @DisplayName("getAverageScore()에서 studentRepository.getStudent()를 한 번, scoreRepository.getScore()를 student의 course 개수만큼 호출한다.")
    void testGetAverageScore_HappyCase_VerifyNumberOfInteractions_Success() {
        // given
        final int studentID = 1;
        final Student trey = new Student().setId(studentID).setName("Trey").setMajor("Computer Engineering")
                .setCourses(List.of(KOREAN, ENGLISH, MATH, SCIENCE));

        Mockito.when(studentRepository.getStudent(1))
                .thenReturn(Optional.of(trey));

        Mockito.when(scoreRepository.getScore(studentID, 1))
                .thenReturn(Optional.of(new Score().setCourse(KOREAN).setScore(100)));
        Mockito.when(scoreRepository.getScore(studentID, 2))
                .thenReturn(Optional.of(new Score().setCourse(ENGLISH).setScore(90)));
        Mockito.when(scoreRepository.getScore(studentID, 3))
                .thenReturn(Optional.of(new Score().setCourse(MATH).setScore(80)));
        Mockito.when(scoreRepository.getScore(studentID, 4))
                .thenReturn(Optional.of(new Score().setCourse(SCIENCE).setScore(70)));

        //when & then
        Assertions.assertEquals(85, transcriptService.getAverageScore(studentID));


        //2 given
        final Student cyh = new Student().setId(studentID).setName("cyh").setMajor("Computer Engineering")
                .setCourses(List.of(KOREAN, ENGLISH, MATH));

        Mockito.when(studentRepository.getStudent(1))
                .thenReturn(Optional.of(cyh));

        Mockito.when(scoreRepository.getScore(studentID, 1))
                .thenReturn(Optional.of(new Score().setCourse(KOREAN).setScore(100)));
        Mockito.when(scoreRepository.getScore(studentID, 2))
                .thenReturn(Optional.of(new Score().setCourse(ENGLISH).setScore(90)));
        Mockito.when(scoreRepository.getScore(studentID, 3))
                .thenReturn(Optional.of(new Score().setCourse(MATH).setScore(80)));

        //2 when & then
        Assertions.assertEquals(90, transcriptService.getAverageScore(studentID));


        //verify
        Mockito.verify(transcriptService, Mockito.times(2)).getAverageScore(1);
        Mockito.verify(scoreRepository, Mockito.times(1)).getScore(studentID, 4);
    }

    @Test
    @DisplayName("scoreRepository로부터 학생의 Score를 하나라도 찾을 수 없는 경우, getAverageScore()는 NoSuchScoreException을 Throw 한다.")
    void testGetAverageScore_ScoreNotExist_ThrowNoSuchScoreException_Error() {
        // given
        final int studentID = 1;
        final Student trey = new Student().setId(studentID).setName("Trey").setMajor("Computer Engineering")
                .setCourses(List.of(KOREAN, ENGLISH, MATH, SCIENCE));

        Mockito.when(studentRepository.getStudent(1))
                .thenReturn(Optional.of(trey));

        Mockito.when(scoreRepository.getScore(studentID, 1))
                .thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(NoSuchScoreException.class, () -> transcriptService.getAverageScore(studentID));
    }

    @Test
    @DisplayName("getRankedStudentAsc()를 호출하면, 입력으로 주어진 course를 수강하는 모든 학생들의 리스트를 성적의 내림차순으로 리턴한다.")
    void testGetRankedStudentsAsc_HappyCase_VerifyReturnedValueAndInteractions_Success() {
        // given
        final int studentID = 1;
        final Student trey = new Student().setId(studentID).setName("Trey").setMajor("Computer Engineering")
                .setCourses(List.of(KOREAN, ENGLISH));
        final int studentID2 = 2;
        final Student cyh = new Student().setId(studentID2).setName("cyh").setMajor("Computer Engineering")
                .setCourses(List.of(KOREAN, ENGLISH));
        final int studentID3 = 3;
        final Student foo = new Student().setId(studentID3).setName("foo").setMajor("Computer Engineering")
                .setCourses(List.of(KOREAN, ENGLISH));

        Mockito.when(courseRepository.getCourse(1))
                .thenReturn(Optional.of(KOREAN));
        Mockito.when(courseRepository.getCourse(2))
                .thenReturn(Optional.of(ENGLISH));

        //when
        Map<Integer,Score> studentIDToScore = new HashMap<>();
        studentIDToScore.put(studentID, new Score().setCourse(KOREAN).setScore(10));
        studentIDToScore.put(studentID2, new Score().setCourse(KOREAN).setScore(20));
        studentIDToScore.put(studentID3, new Score().setCourse(KOREAN).setScore(30));

        Mockito.when(scoreRepository.getScores(1))
                .thenReturn(studentIDToScore);
        Mockito.when(scoreRepository.getScores(1))
                .thenReturn(studentIDToScore);
        Mockito.when(scoreRepository.getScores(1))
                .thenReturn(studentIDToScore);


        Map<Integer,Score> studentIDToScore2 = new HashMap<>();
        studentIDToScore2.put(studentID, new Score().setCourse(ENGLISH).setScore(90));
        studentIDToScore2.put(studentID2, new Score().setCourse(ENGLISH).setScore(80));
        studentIDToScore2.put(studentID3, new Score().setCourse(ENGLISH).setScore(70));

        Mockito.when(scoreRepository.getScores(2))
                .thenReturn(studentIDToScore2);
        Mockito.when(scoreRepository.getScores(2))
                .thenReturn(studentIDToScore2);
        Mockito.when(scoreRepository.getScores(2))
                .thenReturn(studentIDToScore2);

        //2 when
        List<Student> studentsList = new ArrayList<>();
        studentsList.add(trey);
        studentsList.add(cyh);
        studentsList.add(foo);
        Mockito.when(studentRepository.getAllStudents())
                .thenReturn(studentsList);

        // then
        List<Student> rankedStudentsList = transcriptService.getRankedStudentsAsc(1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rankedStudentsList.size(); i++) {
            sb.append(rankedStudentsList.get(i).getId() + " ");
        }
        Assertions.assertEquals(String.valueOf(sb),"3 2 1 ");


        List<Student> rankedStudentsList2 = transcriptService.getRankedStudentsAsc(2);
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < rankedStudentsList2.size(); i++) {
            sb2.append(rankedStudentsList2.get(i).getId() + " ");
        }
        Assertions.assertEquals(String.valueOf(sb2), "1 2 3 ");
    }

    @Test
    @DisplayName("courseRepository에서 입력으로 주어진 courseID로 course를 조회할 수 없으면 NoSuchCourseException을 Throw 한다.")
    void testGetRankedStudentsAsc_CourseNotExist_ThrowNoSuchCourseException_Error() {
        //given
        Mockito.when(courseRepository.getCourse(1))
                .thenReturn(Optional.empty());

        // when & then
        Assertions.assertThrows(NoSuchCourseException.class, () -> transcriptService.getRankedStudentsAsc(1));
    }
}
