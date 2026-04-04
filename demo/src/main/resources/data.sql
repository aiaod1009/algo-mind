-- 关卡数据
INSERT INTO t_level (id, track, sort_order, name, is_unlocked, reward_points, type, question, answer, description) VALUES
(1, 'algo', 1, 'Two Sum', true, 10, 'code', 'Given an array of integers nums and a target value target, find two numbers in the array that sum to target and return their indices.', 'hashmap', 'Classic algorithm problem'),
(2, 'algo', 2, 'Reverse Linked List', false, 15, 'code', 'Given the head of a singly linked list, reverse the list and return the reversed list.', 'iterative', 'Linked list basics'),
(3, 'algo', 3, 'Binary Search', false, 10, 'single', 'What is the time complexity of binary search in a sorted array?', 'O(log n)', 'Algorithm complexity'),
(4, 'ds', 1, 'Stack Properties', true, 10, 'single', 'What type of data structure is a stack?', 'LIFO', 'Data structure basics'),
(5, 'ds', 2, 'Queue Properties', false, 10, 'single', 'What type of data structure is a queue?', 'FIFO', 'Data structure basics'),
(6, 'contest', 1, 'Array Sum', true, 5, 'single', 'Calculate the sum of [1, 2, 3, 4, 5]', '15', 'Basic math');
