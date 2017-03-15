import _ from 'lodash'
function jsonToXml(jsonObj){
    var xmlDoc = document.implementation.createDocument(null, "user");
    _.forEach(jsonObj, (val, key) => {
        const element = xmlDoc.createElement(key);
        element.textContent = val;
        xmlDoc.firstChild.appendChild(element);
    })
    var serializer = new XMLSerializer();
    var xmlString = serializer.serializeToString(xmlDoc);
    console.log(xmlDoc)
    return xmlString;
}

function xmlToJsonArray(data, elementName){
    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(data, 'text/xml');
    const result = _.map(xmlDoc.getElementsByTagName(elementName), userElement => {
        return _.reduce(userElement.children, (acc, child)=>{
            acc[child.tagName] = child.textContent
            return acc
        }, {})
    })
    return result;
}

function getTasks(callback){
    $.get('tasks', (data, status) => {
        window.store.tasks = xmlToJsonArray(data, 'task')
        callback();
    })
}

function getTask(taskId, callback){
    $.get('tasks/'+taskId, (data, status) => {
        const usersArray = xmlToJsonArray(data, 'user')
        window.store.task = {
            task: xmlToJsonArray(data, 'task')[0],
            taskTypes: xmlToJsonArray(data, 'taskType'),
            products: xmlToJsonArray(data, 'taskType'),
            environments: xmlToJsonArray(data, 'environment'),
            users: xmlToJsonArray(data, 'user')
        }
        callback()
    })
}

function createUser(user, callback){
    console.log(jsonToXml(user));
    $.ajax({
        type: 'POST',
        url: 'users/create',
        data: jsonToXml(user),
        success: callback,
        contentType: "application/xml",
        dataType: 'json'
    });
}

function getUsers(callback) {
    $.get('users', (data, status) => {
        window.store.users = xmlToJsonArray(data, 'user')
        callback();
    })
}

function updateUser() {

}

module.exports = {
    createUser,
    getUsers,
    getTask,
    getTasks,
    updateUser
}