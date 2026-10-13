package za.co.eduvos.codebridge.data

/**
 * @param entryPoint     name of the function the user must define (also used by the sandbox)
 * @param testCasesJson  suite in the sandbox format: {"entryPoint":"...","cases":[{"input":"...","expected":"..."}]}
 *                       where input is the argument list as written inside a call.
 */
data class ProblemDetail(
    val id: Int,
    val title: String,
    val category: String,
    val difficulty: String,
    val description: String,
    val example1Input: String,
    val example1Output: String,
    val example2Input: String,
    val example2Output: String,
    val entryPoint: String,
    val initialCode: String,
    val testCasesJson: String,
    val totalAttempts: Int = 0,
    val successRate: String = "0%"
)

object ProblemRepository {

    val problems = listOf(
        ProblemDetail(
            id = 1,
            title = "Two Sum",
            category = "Arrays",
            difficulty = "Easy",
            description = "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
            example1Input = "nums = [2,7,11,15], target = 9",
            example1Output = "[0,1]",
            example2Input = "nums = [3,2,4], target = 6",
            example2Output = "[1,2]",
            entryPoint = "twoSum",
            initialCode = "function twoSum(nums, target) {\n  // Write your solution here\n}",
            testCasesJson = """{"entryPoint":"twoSum","cases":[
                {"input":"[2,7,11,15], 9","expected":"[0,1]"},
                {"input":"[3,2,4], 6","expected":"[1,2]"},
                {"input":"[3,3], 6","expected":"[0,1]"}]}"""
        ),
        ProblemDetail(
            id = 2,
            title = "Reverse Linked List",
            category = "Linked Lists",
            difficulty = "Medium",
            description = "Reverse a singly linked list and return the reversed list. The list is passed as an array of values and the result is returned as an array.",
            example1Input = "head = [1,2,3,4,5]",
            example1Output = "[5,4,3,2,1]",
            example2Input = "head = [1,2]",
            example2Output = "[2,1]",
            entryPoint = "reverseList",
            initialCode = "function reverseList(head) {\n  // head is an array of values\n  // Write your solution here\n}",
            testCasesJson = """{"entryPoint":"reverseList","cases":[
                {"input":"[1,2,3,4,5]","expected":"[5,4,3,2,1]"},
                {"input":"[1,2]","expected":"[2,1]"},
                {"input":"[]","expected":"[]"}]}"""
        ),
        ProblemDetail(
            id = 3,
            title = "Valid Parentheses",
            category = "Stacks",
            difficulty = "Easy",
            description = "Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.",
            example1Input = "s = \"()[]{}\"",
            example1Output = "true",
            example2Input = "s = \"(]\"",
            example2Output = "false",
            entryPoint = "isValid",
            initialCode = "function isValid(s) {\n  // Write your solution here\n}",
            testCasesJson = """{"entryPoint":"isValid","cases":[
                {"input":"\"()[]{}\"","expected":"true"},
                {"input":"\"(]\"","expected":"false"},
                {"input":"\"([)]\"","expected":"false"}]}"""
        ),
        ProblemDetail(
            id = 4,
            title = "Merge Two Sorted Lists",
            category = "Linked Lists",
            difficulty = "Medium",
            description = "Merge two sorted lists into one sorted list. Both lists are passed as arrays and the merged list is returned as an array.",
            example1Input = "list1 = [1,2,4], list2 = [1,3,4]",
            example1Output = "[1,1,2,3,4,4]",
            example2Input = "list1 = [], list2 = []",
            example2Output = "[]",
            entryPoint = "mergeTwoLists",
            initialCode = "function mergeTwoLists(list1, list2) {\n  // Both lists are arrays of values\n  // Write your solution here\n}",
            testCasesJson = """{"entryPoint":"mergeTwoLists","cases":[
                {"input":"[1,2,4], [1,3,4]","expected":"[1,1,2,3,4,4]"},
                {"input":"[], []","expected":"[]"},
                {"input":"[], [0]","expected":"[0]"}]}"""
        ),
        ProblemDetail(
            id = 5,
            title = "Container With Most Water",
            category = "Arrays",
            difficulty = "Medium",
            description = "Find two lines that together with the x-axis form a container such that the container contains the most water.",
            example1Input = "height = [1,8,6,2,5,4,8,3,7]",
            example1Output = "49",
            example2Input = "height = [1,1]",
            example2Output = "1",
            entryPoint = "maxArea",
            initialCode = "function maxArea(height) {\n  // Write your solution here\n}",
            testCasesJson = """{"entryPoint":"maxArea","cases":[
                {"input":"[1,8,6,2,5,4,8,3,7]","expected":"49"},
                {"input":"[1,1]","expected":"1"},
                {"input":"[4,3,2,1,4]","expected":"16"}]}"""
        )
    )

    fun getById(id: Int): ProblemDetail {
        return problems.find { it.id == id } ?: problems.first()
    }
}
