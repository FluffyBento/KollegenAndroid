





#ifndef SDL_test_h_arness_h
#define SDL_test_h_arness_h

#include <SDL3/SDL_stdinc.h>
#include <SDL3/SDL_test_common.h> 

#include <SDL3/SDL_begin_code.h>

#ifdef __cplusplus
extern "C" {
#endif


#define TEST_ENABLED  1
#define TEST_DISABLED 0


#define TEST_ABORTED        -1
#define TEST_STARTED         0
#define TEST_COMPLETED       1
#define TEST_SKIPPED         2


#define TEST_RESULT_PASSED              0
#define TEST_RESULT_FAILED              1
#define TEST_RESULT_NO_ASSERT           2
#define TEST_RESULT_SKIPPED             3
#define TEST_RESULT_SETUP_FAILURE       4


typedef void (SDLCALL *SDLTest_TestCaseSetUpFp)(void **arg);


typedef int (SDLCALL *SDLTest_TestCaseFp)(void *arg);


typedef void  (SDLCALL *SDLTest_TestCaseTearDownFp)(void *arg);


typedef struct SDLTest_TestCaseReference {
    
    SDLTest_TestCaseFp testCase;
    
    const char *name;
    
    const char *description;
    
    int enabled;
} SDLTest_TestCaseReference;


typedef struct SDLTest_TestSuiteReference {
    
    const char *name;
    
    SDLTest_TestCaseSetUpFp testSetUp;
    
    const SDLTest_TestCaseReference **testCases;
    
    SDLTest_TestCaseTearDownFp testTearDown;
} SDLTest_TestSuiteReference;



char * SDLCALL SDLTest_GenerateRunSeed(char *buffer, int length);


typedef struct SDLTest_TestSuiteRunner SDLTest_TestSuiteRunner;


SDLTest_TestSuiteRunner * SDLCALL SDLTest_CreateTestSuiteRunner(SDLTest_CommonState *state, SDLTest_TestSuiteReference *testSuites[]);


void SDLCALL SDLTest_DestroyTestSuiteRunner(SDLTest_TestSuiteRunner *runner);


int SDLCALL SDLTest_ExecuteTestSuiteRunner(SDLTest_TestSuiteRunner *runner);


#ifdef __cplusplus
}
#endif
#include <SDL3/SDL_close_code.h>

#endif 
