### TO-DO List ###
1. GUI:
* List the name and status of jobs in a table
* Provider a button to show the result of each completed job
* List the name and status of workers in a table
* Allow to specify the job name when add a job
* Allow to specify a deadline time when adding a job (extra)
* Allow to specify a maximum memory use when adding a job (extra)
* GUI failure handling (user input validation)
2. Use SSL in all communication between master and worker
3. Deploy workers to Nectar cloud
4. Exception and failure handling
5. Test the concurrency
6. Worker selection (job placement) with round robin or according to workload from each worker (extra)
7. Handle the deadline time and maximum memory when executing the job (extra)