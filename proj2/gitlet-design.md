# Gitlet Design Document

**Name**: Alex

## Classes and Data Structures

### Class 1 - Commit

#### Private Fields

1. **String timestamp**: The exact timestamp when the commit object is created by gitlet
2. **String message**: The message that is passed in by user by gitlet commit -m "message"
3. **List\<String\> parent**: The SHAIDs' for all parent commit objects
4. **String UID**: Universal Hash SHA1 ID for each commit object
5. **String TYPE**: Indicate whether it is a commit or a blob


### Class 2 - Blob

#### Private Fields

1. **String UID**: SHA1 ID for Blob Object
2. **File filePointer**: Pointer to the file descriptor File(filepath), used for later filereading
3. **String fileName**: The name of the file being pointed to, could be the same across different versions
4. **String TYPE**: Distinguish between Commit and Blob Object

### Class 3 - Stage

### Fields

1. pathToBlobIDAddition
2. pathToBlobIDRemoval


## Algorithms




## Persistence
The file structure is the following:
- refs        DIR
  - heads     DIR
    - master  FILE
    - branchName1 FILE
    - branchName2 FILE
  - remotes
    - origin  FILE
- objects     DIR
    - 02      Serialized Object
    - e3
    - e3
    - etc
- HEAD     FILE, stores ref: refs/heads/branch_name
- index    FILE

