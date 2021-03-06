import React from 'react'
import _ from 'lodash'
import FilterableTable from './filterableTable.jsx'
import consts from '../consts'
//see Server here: http://bootsnipp.com/snippets/featured/panel-table-with-filters-per-column
const columns = consts.userFields;
class users extends React.Component {
    constructor(){
        super()
        this.tableRowClicked = this.tableRowClicked.bind(this)
        this.addUser = this.addUser.bind(this)
    }
    tableRowClicked(user) {
        this.props.setCurrentUser(user)
    }

    addUser() {
        this.props.onAddingUser()
    }

    render() {
        return (
            <div className="container">
                <h1 style={{textAlign: 'center', fontFamily:'arialBlack', fontSize:'5em', marginTop:'40px'}}>User List</h1>
                <FilterableTable
                    onAddItem={this.addUser}
                    addItemDisplayName='Add User'
                    columns={columns}
                    panelTitle='Users'
                    onTableRowClicked={this.tableRowClicked}
                    items={this.props.users}>
                </FilterableTable>
            </div>
        )
    }
}
module.exports = users
