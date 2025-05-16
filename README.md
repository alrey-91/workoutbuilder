# **Workout Builder Web Application**

Alex Reyes

**Project Description:**

Many people at the gym or at home struggle to find the most optimal exercises for their workouts that they either find from online videos, websites, or just straight up winging it in the gym to get in shape or to get stronger. With the workout builder web application, users will be able to browse and filter through a variety of different exercises for their goals, save and/or favorite specific exercises, and create a personalized workout routine that they are most comfortable with. The application can be used for people from all levels of fitness to help them build the right workout for them.

**Requirements:**  
Functional Requirements:

* Application must be able to retrieve and display a list of exercises for different body groups/muscles  
* List of exercises to be retrieved from some database  
* Application must be able to filter and return list of exercises based on a exercises bodygroup filter from the user  
* Users of application must be able to login to access saved workout lists, with a username and password  
* Workout list management: users can add, view and remove exercises from their workout list  
* Application must be able to save workouts created by the user (Most likely in a database)

	Non-functional Requirements:

* Application must have a good UI  
* App should be fast and responsive, users should be able to quickly filter and retrieve exercises and their saved workouts in reasonable time  
* User sessions should be secured, as well as their login credentials  
* Other features: Users can rename their workouts to whatever they want  
* Users can add to their workouts how many sets and reps they should do per exercise

**Technical Specifications:**  
      Back-End:

* ~~Handles user login requests~~  (*scrapped*)  
* ~~Track login attempts using HttpSession, store username~~  
* User can register and login with new credentials made on signup, user session persistence  
* After authentication redirect to exercises page  
* Provide list of exercises in JSON format from external API  
* User can filter out different exercises given 1-2 parameters  
* Data is stored in a database  
* User can add or remove exercises from their saved workout list, view all their lists  
* User can add custom sets or reps when adding exercises to workout  
* User can add or delete workoutlists  
* Using: maven project with dropwizard, JDBI v3, jersey

TODO:

* ~~Add metadata for exercises such as sets, reps etc. in users workoutlist~~  
* ~~Implement DAOs for current POJOs~~    
* Try and get all possible exercises from external API   
* ~~Log out endpoint?~~
* Add authentication, logging
* Reimplement postgres

**Endpoints: (Spring Boot)**  
Uses in-memory H2 database, so it resets every run. Use /signup, /exercises endpoints to add new user and exercises data into db.  

Update hw7: Now uses local postgresql database

Completed full unit and integration testing with jacoco. Achieved 90.4% coverage.

/Login:

* Method: POST, GET for /login.html form  
* Request: username and password credentials, must be registered user OR: oauth2 login with google  
* Response: success or fail. on success, set user session  
* After logging in, pertain user session with cookie

/signup:

* Method: POST  
* Request: username and password credentials to register. Username must not be taken in db already, fields cannot be empty  
* Response: success or fail. On success: user registered, added to db. 

/logout:

* Method: POST  
* Request: No request body needed, just have to be logged in session  
* Response: If a user is logged in session, successfully logout and invalidate session, fail if no user is logged in

/exercises{?muscle={muscle}\&type={type}:

* Method: GET  
* Response data in JSON format. Ex: an exercise with name, muscle group, type and instructions. Parameters for muscle and type  
* Parameters optional. No params respond with ALL possible exercises in db, external api. Params type, muscle for getting specific exercises, can use one or the other, or both

/workouts:

* (user must be logged in to access /workouts endpoints)  
* **Method: GET**: (retrieve user sessions workoutlist)  
* Parameters: ?workoutId={w.id}  gets one workout by id that is tied to user. If no parameter(/workouts and thats it), get list of all the users workouts, ordered by id

/workouts/add-exercise:

* **Method: PUT**: (Add exercises to user workoutlist)  
* Params when using PUT: sets={num of sets} and/or reps={num of reps}. If either params is empty, default to 3 sets and 10 reps for exercise.  
* Request body: workoutId (workout list to modify), exerciseId (exercise to add)  
* Response: success: exercise added to workout with metadata, fail: was not added. Either workoutid or exerciseid json req body blank, exercise already exists in workout list, or ids dont exist.

/workouts/remove-exercise:

* **Method: DELETE**: (If exercise specified is in workoutList, remove it)  
* Request body: same as when method is PUT. workoutId (list to modify) and exerciseId (exercise to be removed)  
* Response: on success: exercise is successfully removed. On fail: nothing.

/workouts/new-workout

* Methods: POST: create a new empty workout list for logged in user  
* Req data (As JSON): “name”: “name of new workout”  
* Response: “success”. will create a new empty workout arrlist for user in session, fail: if name field is empty. User can have workouts with the same name, no constraint on that.  
* Name in request body cannot  be empty


/workouts/delete?workoutId={workoutId}

* Method: DELETE  
* Delete a workout list for a user given a workoutId in the parameter.   
* workoutId param must not be empty, and it must exist. On success: delete from database, along with the workouts exercises in workout\_exercises table

| Endpoint | RESTful methods | Request Parameters (or JSON req body) | Response |
| :---- | :---- | :---- | :---- |
| /login | POST | Username, password (JSON body) | JSON: success (boolean) message string, or failed. Set session to users id |
| /signup | POST | New username, password, new user added to DB. same json format as /login | JSON: if successful signup: success, user can login, if not (blank parameters or username taken) not succesful |
| /logout | POST | Current user must be in session | Success: logout and invalidate session, fail if no client is logged in |
| /exercises | GET | ?muscle=”muscle” and/or ?/\&Type=”type” (for URI to filter exercises) | JSON: List of exercises fetched from API (up to 10), then workouts in db. Success \= trueIf no workouts found, list will be empty and “success”: will be false. Return error if no response from external API. no params will now show ALL exercises in DB |
| /workouts Add exercise: /add-exercise Remove exercise: /remove-exercise | GET, PUT, DELETE | For PUT: valid JSON format workoutid, exerciseid. Sets and reps optional in parametersFor DELETE: in JSON: exercise that exists within workoutist. Workoutid, exerciseidParams (GET): workoutId={id}, Get one workout by id Params (PUT): sets={sets}, reps={reps}. User must be logged in or return 401error | JSON:Users workoutLists or single workoutlist given workoutId param.PUT:Success or fail. insert exercise on successful request. |
| /workouts/new-workout | POST | JSON body: Name of new workoutUser must be logged in. 401 error | JSON response: Success if named correctly, False, return 400 error |
| /workouts/delete?workoutId={workoutId} | DELETE | No json body, just param. Delete user workoutlist given paramMUST have to specify a workoutId in paramUser must be logged in | Json response: on success, workout deleted successfully. Fail: 404 error if workoutId param is null, json response fail if workoutid does not exist for user. |

 
