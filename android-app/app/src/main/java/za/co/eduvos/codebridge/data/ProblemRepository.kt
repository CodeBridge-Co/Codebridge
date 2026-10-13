package za.co.eduvos.codebridge.data

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
    val initialCode: String,
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
            initialCode = "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Write your solution here\n    }\n}"
        ),
        ProblemDetail(
            id = 2,
            title = "Reverse Linked List",
            category = "Linked Lists",
            difficulty = "Medium",
            description = "Given the head of a singly linked list, reverse the list, and return the reversed list.",
            example1Input = "head = [1,2,3,4,5]",
            example1Output = "[5,4,3,2,1]",
            example2Input = "head = [1,2]",
            example2Output = "[2,1]",
            initialCode = "class Solution {\n    public ListNode reverseList(ListNode head) {\n        // Write your solution here\n    }\n}"
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
            initialCode = "class Solution {\n    public boolean isValid(String s) {\n        // Write your solution here\n    }\n}"
        ),
        ProblemDetail(
            id = 4,
            title = "Merge Two Sorted Lists",
            category = "Linked Lists",
            difficulty = "Medium",
            description = "You are given the heads of two sorted linked lists list1 and list2. Merge the two lists into one sorted list.",
            example1Input = "list1 = [1,2,4], list2 = [1,3,4]",
            example1Output = "[1,1,2,3,4,4]",
            example2Input = "list1 = [], list2 = []",
            example2Output = "[]",
            initialCode = "class Solution {\n    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {\n        // Write your solution here\n    }\n}"
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
            initialCode = "class Solution {\n    public int maxArea(int[] height) {\n        // Write your solution here\n    }\n}"
        )
    )

    fun getById(id: Int): ProblemDetail {
        return problems.find { it.id == id } ?: problems.first()
    }
}
