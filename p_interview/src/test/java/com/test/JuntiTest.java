package com.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.interview.TestService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JuntiTest {
	@Autowire
	private TestService testService;
	
	@Test
	public void test(){
		testService.SayHi();
	}
	
}
