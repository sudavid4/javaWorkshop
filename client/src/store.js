const store = {
    tasks: {
        tasks: null,
        metaData: null,
        filter:null,
        currentTask: null
    },
    users: {
        users: null,
        currentUser: null,
        metaData: null
    },
    activeMenu: 'login',
    userType: null
}

const setTasks = (tasks) => store.tasks.tasks = tasks;
const getTasks = () =>  store.tasks.tasks;

const setTasksMetadata = (tasksMetadata) => store.tasks.metaData = tasksMetadata
const getTasksMetadata = () => store.tasks.metaData

const setTasksFilter = (filter) => store.tasks.filter = filter;
const getTasksFilter = () =>  store.tasks.filter;

const setCurrentTask = (task) => store.tasks.currentTask = task
const getCurrentTask = () =>  store.tasks.currentTask

const setActiveMenu = (activeMenu) => {
    if (activeMenu === 'login') {
        setUserType(null)
    }
    store.activeMenu = activeMenu
}
const getActiveMenu = () => store.activeMenu

const setUsers = (users) => store.users.users = users;
const getUsers = () =>  store.users.users;

const setUsersMetadata = (usersMetadata) => store.users.metaData = usersMetadata
const getUsersMetadata = () => store.users.metaData

const setUsersFilter = (filter) => store.users.filter = filter;
const getUsersFilter = () =>  store.users.filter;

const setCurrentUser = (user) => store.users.currentUser = user
const getCurrentUser = () =>  store.users.currentUser

const setOops = ({status, statusText, responseText}) => store.oops = {status, statusText, responseText}
const getOops = () => store.oops

const setUserType = userType => store.userType = userType
const getUserType = () => store.userType
const retVal = {
    setActiveMenu,
    getActiveMenu,
    setCurrentTask,
    getCurrentTask,
    setTasks,
    getTasks,
    setTasksMetadata,
    getTasksMetadata,
    setTasksFilter,
    getTasksFilter,
    setUsers,
    getUsers,
    setUsersMetadata,
    getUsersMetadata,
    setUsersFilter,
    getUsersFilter,
    setCurrentUser,
    getCurrentUser,
    setOops,
    getOops,
    setUserType,
    getUserType
}
window.store = store
window.Store = retVal
module.exports = retVal
