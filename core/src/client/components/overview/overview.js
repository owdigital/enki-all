/*
   This file is part of Enki.
  
   Copyright © 2016 - 2019 Oliver Wyman Ltd.
  
   Enki is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
  
   Enki is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
  
   You should have received a copy of the GNU General Public License
   along with Enki.  If not, see <https://www.gnu.org/licenses/>
*/

import moment from 'moment';
import Vue from 'vue';
import helper from './overview-helper';
import $ from 'jquery';
import ExpiredConsent from './expired-consent.json';
import ForgetConsent from './forget-consent.json';
import SoonConsent from './soon-consent.json';
import HighchartsVue from 'highcharts-vue';
import Highcharts from 'highcharts';

import HighchartsMore from 'highcharts/highcharts-more';
import annotationsInit from 'highcharts/modules/annotations';
import seriesLabelInit from 'highcharts/modules/series-label';

import StreamGraph from './streamgraph';

seriesLabelInit(Highcharts);
annotationsInit(Highcharts);
HighchartsMore(Highcharts);
StreamGraph(Highcharts);
Vue.use(HighchartsVue);


function setupJQuery() {


}


function renderChart() {
  return {
    chart: {
      type: 'streamgraph',
      marginBottom: 0,
      zoomType: 'x'
    },

    colors: [
      '#dc3545',
      '#ffc107',
      '#6c757d'
    ],

    title: {
      floating: true,
      align: 'left',
      text: 'Breakdown of consent cases by type'
    },
    subtitle: {
      floating: true,
      align: 'left',
      x: 50,
      y: 30,
      text: 'Past 25 weeks'
    },

    xAxis: {
      maxPadding: 0,
      type: 'category',
      crosshair: true,
      categories: [
        '21 MAY 18',
        '28 MAY 18',
        '04 JUN 18',
        '11 JUN 18',
        '18 JUN 18',
        '25 JUN 18',
        '02 JUL 18',
        '09 JUL 18',
        '16 JUL 18',
        '23 JUL 18',
        '30 JUL 18',
        '06 AUG 18',
        '13 AUG 18',
        '20 AUG 18',
        '27 AUG 18',
        '03 SEP 18',
        '10 SEP 18',
        '17 SEP 18',
        '24 SEP 18',
        '01 OCT 18',
        '08 OCT 18',
        '15 OCT 18',
        '22 OCT 18',
        '29 OCT 18',
        '05 NOV 18'
      ],
      labels: {
        align: 'left',
        reserveSpace: true,
        rotation: 270,
        y: -30,
        x: 13
      },
      lineWidth: 0,
      margin: 0,
      tickWidth: 0
    },

    yAxis: {
      visible: false,
      startOnTick: false,
      endOnTick: false
    },

    legend: {
      enabled: true,
      x: 500,
      y: -325
    },

    annotations: [{
      labels: [{
        point: {
          x: 1,
          xAxis: 0,
          y: 0,
          yAxis: 0
        },
        text: 'GDPR enforcable'
      }],
      labelOptions: {
        backgroundColor: 'rgba(255,255,255,0.5)',
        borderColor: 'silver'
      }
    }],

    plotOptions: {
      series: {
        label: {
          minFontSize: 5,
          maxFontSize: 15,
          style: {
            color: 'rgba(255,255,255,0.75)'
          }
        }
      }
    },

    series: [{
      "type": "streamgraph",
      "name": "Right to be forgotten requests",
      "data": [
        765, 8495, 6375, 34185, 68532, 90158, 71710, 83648, 95581, 74231, 45310, 68295, 59686, 42234, 56630,
        68171, 59224, 43083, 30824, 29716, 46580, 42483, 57159, 61275, 39684
      ]
    }, {
      "type": "streamgraph",
      "name": "Expired consent cases",
      "data": [
        79863, 98765, 984613, 821665, 764516, 767651, 654123, 653324, 590878, 543218, 505247, 432182, 318416,
        286781, 297946, 232417, 161684, 132745, 115439, 94653, 86463, 78451, 68154, 49563, 30679
      ]
    }, {
      "type": "streamgraph",
      "name": "Consent renewals",
      "data": [
        98765, 984613, 821665, 794516, 767651, 654123, 653324, 598781, 543218, 505347, 432782, 328413, 286781,
        297946, 232417, 161684, 132745, 115439, 94653, 106463, 128451, 168154, 179563, 160679, 199493
      ]
    }]
  };
}

function formatDate(value) {
  if (value) {
    const val = String(value);
    const dateFormat = 'DD MMM YYYY';
        // using utc to prevent timezone offsets
    return moment(val).utc().format(dateFormat);
  }
}

export default {
  data() {
    return {
      searchText: '',
      bankUrl: '',
      banks: [],
      piiTypes: [],
      shareData: [],
      metaData: [],
      byBankData: [],
      byPIITypeData: [],
      selectedBank: '',
      selectedPIIType: '',
      ExpiredConsent: ExpiredConsent,
      SoonConsent: SoonConsent,
      ForgetConsent: ForgetConsent,
      Regions: [
                {text: 'Select One', value: null},
        'Europe', 'North America', 'South America', 'Asia', 'Africa'
      ],
      Products: [
                {text: 'Select One', value: null},
        'Savings', 'Mortgage', 'Loan', 'Current Account'
      ],
      byPIITypeColumns: [
        helper.column('Who', 'name', true),
        helper.column('Purpose', 'purpose', true),
        helper.dateColumn('Since', 'start_date'),
        helper.dateColumn('My consent expires on', 'end_date'),
        helper.column('', ''),
      ],
      byBankColumns: [
        helper.column('Detail', 'piitype', true),
        helper.column('Purpose', 'purpose', true),
        helper.dateColumn('Since', 'start_date'),
        helper.dateColumn('My consent expires on', 'end_date'),
        helper.column('', ''),
      ],
      editedItem: '',
      chartOpts: renderChart()
    };
  },
  beforeMount() {
    this.fetchData();
    console.log('beforeMount' + this);
  },
  mounted: function () {
    setupJQuery();
  },
  methods: {
    fetchData() {
      Vue.http.get('/assertions', {params: {credentials: 'true'}}).then(response => {
        if (this.isValidResponseData(response.body)) {
          this.shareData = response.body.shareAssertions;
          this.metaData = response.body.metadataAssertions;
          this.banks = this.shareData.map((bank) => {
            console.log('line 141, fetchData' + bank.name);
            return bank.name;
          }).filter((elem, pos, arr) => {
            return arr.indexOf(elem) === pos;
          }).sort();

          this.piiTypes = this.shareData.map((piiType) => {
            return piiType.piitype;
          }).filter((elem, pos, arr) => {
            return arr.indexOf(elem) === pos;
          }).sort();
        }
      }, () => {
                // Not successful
        console.log('Assertions not fetched successfully');
      });
    },
    selectedBankChanged() {
      this.byBankData = this.shareData.filter(item => item.name === this.selectedBank);
      const bankUrls = window.app.context.bankUrls;
      const bankUrlData = bankUrls.find(urls => urls.name === this.selectedBank);
      console.log('url data ' + JSON.stringify(bankUrlData));
      this.bankUrl = bankUrlData.url;
    },
    selectedPIITypeChanged() {
      this.byPIITypeData = this.shareData.filter(item => item.piitype === this.selectedPIIType);
    },
    isValidResponseData(data) {
      let isValid = false;
      if (typeof data === 'object') {
        if (Array.isArray(data.shareAssertions) || Array.isArray(data.metadataAssertions)) {
          isValid = true;
        }
      }
      console.log('line 143, isValidResponseData ' + isValid);
      return isValid;
    },
    deleteRow(props) {
      this.editedItem = props.row;
      this.$modal.show('modal-delete');
    },
    hideDeleteModal() {
      this.editedItem = '';
      this.$modal.hide('modal-delete');
    },
    editItem() {
            //not implemented yet
      console.log("Editing not implemented, yet.");
    },
    deleteItem() {
            //not implemented yet
      console.log("Deleting not implemented, yet.");
    },
  },
  filters: {
    formatDate: formatDate
  }
};

//test table writer
