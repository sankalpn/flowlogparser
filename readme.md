# flowlogparser
A utility to parse Amazon VPC flow logs.
## Assumptions
1. Tag CSV is assumed to fit memory.
2. Tag CSV is assumed to be RFC-4180 complaint.
3. Tag CSV is assumed to not contain duplicates.
4. For flow logs, only the fields in version 2 are supported.
5. Only the log lines with all fields fully computed are supported. No support, for example, src IP being '-' (not computed).
6. IP address was not part of any summary requirements, so assumed to be Ipv4 and no error checking is implemented other than the regex "[0-9]{1,3}+\.[0-9]{1,3}+\.[0-9]{1,3}+\.[0-9]{1,3}+".
7. No error checking for port being less than 65535.
8. No error checking for protocol being less than 255.
9. Example from recruiter email seems to have destination and source ports in opposite sequence from AWS documentation. Code follows AWS documentation. 
10. Analyzed counts for tags and port/protocol pair are assumed to fit Java int.
11. Result file examples looked like CSVs, but this doesn't split into individual CSV files.
12. Port/Protocol count did not mention source port or destination port, so returning both separately.

This translates to the regex from LogLineParser.  
Note that this regex can be extended later to support more versions/fields. Also, wrapper classes can be written to validate fields like port.
## Components
### LogLineParser
Utility to parse log line into different fields. Regex capture groups have self-explanatory names.
### TagMapExtractor
Utility to parse CSV file containing tags into an in-memory map.
### ParserFlow
Parses log file and analyzes for tag count and port/protocol count. Processes lines from log file in parallel.
## Design considerations
The reason tag file is read into memory separately before ParserFlow is executed is to account for the same tag file being used for multiple log files.  
In order to scale this in terms of data processed, tag file could be extended to a DB table and log file could be streamed over message queues.
## Development and testing
### Env
Install openjdk 21 and Download IntelliJ Idea (community or ultimate, both work)
### Download repo
In IntelliJ, select File -> New -> Project from Version Control, then use repo link to clone.  
Make sure that JDK in use is version 21.
### Set up run configuration
Select Application and point to com.illumio.flowlogparser.Main class.  
Set up VM arguments as -Dtag.file=<path_to_tag_csv> -Dlog.file=<path_to_flow_log_file> -Dresult.file=<path_to_result_file>.  
Note that result file should be writable by the user running this process
### Run the application
Some basic runtime metrics will be logged on console.
