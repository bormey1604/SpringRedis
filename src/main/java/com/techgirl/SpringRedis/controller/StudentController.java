package com.techgirl.SpringRedis.controller;import com.fasterxml.jackson.core.JsonProcessingException;import com.fasterxml.jackson.databind.ObjectMapper;import com.techgirl.SpringRedis.model.Response;import com.techgirl.SpringRedis.model.Student;import com.techgirl.SpringRedis.service.StudentService;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.redis.core.RedisTemplate;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import java.util.Optional;import java.util.concurrent.TimeUnit;@RequestMapping("/student_info/v1")@RestControllerpublic class StudentController {    @Autowired    private RedisTemplate<String,String> redisTemplate;    @Autowired    private StudentService studentService;    private final String REDIS_KEY = "STUDENT";    @GetMapping("/students/{student_id}")    public ResponseEntity<Object> getStudentById(@PathVariable Long student_id) throws JsonProcessingException {        try{            String key = REDIS_KEY.concat(":").concat(String.valueOf(student_id));            var cachedStudent = redisTemplate.opsForValue().get(key);            if(cachedStudent == null){                Optional<Student> student = studentService.getStudentById(student_id);                if(student.isPresent()){                    redisTemplate.opsForValue().set(key,student.get().toJson());                    if(redisTemplate.getExpire(key)<0){                        redisTemplate.expire(key, 10,TimeUnit.MINUTES);                    }                    return new ResponseEntity<Object>(new Response("success","student from db",student.get()), HttpStatus.OK);                }                return new ResponseEntity<Object>(new Response("success","student from db not found",null), HttpStatus.NOT_FOUND);            }            ObjectMapper objectMapper= new ObjectMapper();            Student student = objectMapper.readValue(cachedStudent,Student.class);            return new ResponseEntity<Object>(new Response("success","student from cache",student), HttpStatus.OK);        }catch (Exception e){            return new ResponseEntity<Object>(new Response("fail",e.getMessage(),null), HttpStatus.INTERNAL_SERVER_ERROR);        }    }    @PostMapping("/students")    public ResponseEntity<Object> createStudent(@RequestBody Student student) {        studentService.addStudent(student);        return new ResponseEntity<Object>(new Response("success","create successfully",student), HttpStatus.CREATED);    }}